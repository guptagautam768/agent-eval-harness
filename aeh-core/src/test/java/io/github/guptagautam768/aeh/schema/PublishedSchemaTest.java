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
package io.github.guptagautam768.aeh.schema;

import static org.assertj.core.api.Assertions.assertThat;

import com.networknt.schema.Error;
import com.networknt.schema.InputFormat;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;
import io.github.guptagautam768.aeh.io.BaselineStore;
import io.github.guptagautam768.aeh.io.CassetteStore;
import io.github.guptagautam768.aeh.io.JsonDocuments;
import io.github.guptagautam768.aeh.model.ModelFixtures;
import io.github.guptagautam768.aeh.pricing.PriceTables;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class PublishedSchemaTest {

  private static final SchemaRegistry REGISTRY =
      SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);

  @Test
  void publishedSchemasAcceptTheRoundTripDocuments() throws IOException {
    assertValid("cassette-1.0.schema.json", new CassetteStore().toBytes(ModelFixtures.cassette()));
    assertValid("baseline-1.0.schema.json", new BaselineStore().toBytes(ModelFixtures.baseline()));
    assertValid("eval-case-1.0.schema.json", new JsonDocuments().write(ModelFixtures.evalCase()));
    assertValid("result-1.0.schema.json", new JsonDocuments().write(ModelFixtures.evalResult()));
    assertValid("report-1.0.schema.json", new JsonDocuments().write(ModelFixtures.report()));
    assertValid("price-table-1.0.schema.json", PriceTables.write(ModelFixtures.priceTable()));
    assertValid("price-table-1.0.schema.json", PriceTables.write(PriceTables.builtin()));
  }

  private static void assertValid(String schemaFile, byte[] document) throws IOException {
    Path schemaPath = repoRoot().resolve("schemas").resolve(schemaFile);
    Schema schema = REGISTRY.getSchema(Files.readString(schemaPath));
    List<Error> errors =
        schema.validate(new String(document, StandardCharsets.UTF_8), InputFormat.JSON);
    assertThat(errors).as(schemaFile).isEmpty();
  }

  private static Path repoRoot() {
    Path dir = Path.of("").toAbsolutePath();
    while (dir != null && !Files.exists(dir.resolve("settings.gradle.kts"))) {
      dir = dir.getParent();
    }
    if (dir == null) {
      throw new IllegalStateException("Could not find the repository root");
    }
    return dir;
  }
}
