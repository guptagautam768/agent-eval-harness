/*
 * Copyright 2026 Gautam Gupta
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.guptagautam768.aeh.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.guptagautam768.aeh.model.Step;
import io.github.guptagautam768.aeh.model.StepDeserializer;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Deterministic JSON codec: sorted map keys, fixed model property order, 2-space indent, {@code \n}
 * endings, trailing newline, UTF-8, money as plain decimal strings, UTC timestamps with millisecond
 * precision (ADR-0003).
 */
public final class AehJson {

  private static final AehJson SHARED = new AehJson();

  private final ObjectMapper mapper;

  public AehJson() {
    ObjectMapper configured = new ObjectMapper();
    configured.registerModule(module());
    configured.enable(SerializationFeature.INDENT_OUTPUT);
    configured.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
    configured.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    configured.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    configured.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
    this.mapper = configured;
  }

  public static AehJson shared() {
    return SHARED;
  }

  public ObjectMapper mapper() {
    return mapper;
  }

  public byte[] write(Object value) {
    try {
      String text = mapper.writer(printer()).writeValueAsString(value);
      if (!text.endsWith("\n")) {
        text = text + "\n";
      }
      return text.getBytes(StandardCharsets.UTF_8);
    } catch (IOException exception) {
      throw new JsonDocumentException("Could not write JSON", exception);
    }
  }

  public <T> T read(byte[] bytes, Class<T> type) {
    try {
      return mapper.readValue(bytes, type);
    } catch (IOException exception) {
      throw new JsonDocumentException("Could not read " + type.getSimpleName(), exception);
    }
  }

  private static SimpleModule module() {
    SimpleModule module = new SimpleModule("aeh");
    module.addSerializer(BigDecimal.class, new DecimalSerializer());
    module.addDeserializer(BigDecimal.class, new DecimalDeserializer());
    module.addSerializer(Instant.class, new InstantSerializer());
    module.addDeserializer(Instant.class, new InstantDeserializer());
    CanonicalNodeSerializer nodes = new CanonicalNodeSerializer();
    module.addSerializer(JsonNode.class, nodes);
    module.addSerializer(ObjectNode.class, nodes);
    module.addSerializer(ArrayNode.class, nodes);
    module.addDeserializer(Step.class, new StepDeserializer());
    return module;
  }

  private static DefaultPrettyPrinter printer() {
    DefaultPrettyPrinter printer = new DefaultPrettyPrinter();
    DefaultIndenter indenter = new DefaultIndenter("  ", "\n");
    printer.indentObjectsWith(indenter);
    printer.indentArraysWith(indenter);
    return printer;
  }

  private static final class DecimalSerializer extends JsonSerializer<BigDecimal> {
    @Override
    public void serialize(BigDecimal value, JsonGenerator generator, SerializerProvider serializers)
        throws IOException {
      generator.writeString(Decimals.plain(value));
    }
  }

  private static final class DecimalDeserializer extends JsonDeserializer<BigDecimal> {
    @Override
    public BigDecimal deserialize(JsonParser parser, DeserializationContext context)
        throws IOException {
      if (parser.currentToken() == JsonToken.VALUE_NULL) {
        return null;
      }
      if (parser.currentToken().isNumeric()) {
        return Decimals.canonical(parser.getDecimalValue());
      }
      return Decimals.canonical(new BigDecimal(parser.getValueAsString()));
    }
  }

  private static final class InstantSerializer extends JsonSerializer<Instant> {
    private static final DateTimeFormatter FORMAT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);

    @Override
    public void serialize(Instant value, JsonGenerator generator, SerializerProvider serializers)
        throws IOException {
      generator.writeString(FORMAT.format(value.truncatedTo(ChronoUnit.MILLIS)));
    }
  }

  private static final class InstantDeserializer extends JsonDeserializer<Instant> {
    @Override
    public Instant deserialize(JsonParser parser, DeserializationContext context)
        throws IOException {
      return Instant.parse(parser.getValueAsString()).truncatedTo(ChronoUnit.MILLIS);
    }
  }

  /** Writes object keys in sorted order so arbitrary JSON payloads diff stably. */
  private static final class CanonicalNodeSerializer extends JsonSerializer<JsonNode> {
    @Override
    public void serialize(JsonNode value, JsonGenerator generator, SerializerProvider serializers)
        throws IOException {
      write(value, generator);
    }

    private static void write(JsonNode node, JsonGenerator generator) throws IOException {
      if (node == null || node.isNull()) {
        generator.writeNull();
      } else if (node.isObject()) {
        generator.writeStartObject();
        List<String> names = new ArrayList<>();
        node.fieldNames().forEachRemaining(names::add);
        Collections.sort(names);
        for (String name : names) {
          generator.writeFieldName(name);
          write(node.get(name), generator);
        }
        generator.writeEndObject();
      } else if (node.isArray()) {
        generator.writeStartArray();
        for (JsonNode child : node) {
          write(child, generator);
        }
        generator.writeEndArray();
      } else if (node.isTextual()) {
        generator.writeString(node.textValue());
      } else if (node.isIntegralNumber()) {
        generator.writeNumber(node.bigIntegerValue());
      } else if (node.isFloatingPointNumber()) {
        generator.writeNumber(node.decimalValue());
      } else if (node.isBoolean()) {
        generator.writeBoolean(node.booleanValue());
      } else if (node.isBinary()) {
        generator.writeBinary(node.binaryValue());
      } else {
        generator.writeNull();
      }
    }
  }
}
