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
package io.github.guptagautam768.aeh.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.guptagautam768.aeh.io.BaselineStore;
import io.github.guptagautam768.aeh.io.CassetteStore;
import io.github.guptagautam768.aeh.io.JsonDocuments;
import io.github.guptagautam768.aeh.json.VersionedDocument;
import io.github.guptagautam768.aeh.pricing.PriceTable;
import io.github.guptagautam768.aeh.pricing.PriceTables;
import io.github.guptagautam768.aeh.schema.SchemaVersions;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class DataModelRoundTripTest {

  private final CassetteStore cassettes = new CassetteStore();
  private final BaselineStore baselines = new BaselineStore();
  private final JsonDocuments documents = new JsonDocuments();

  @Test
  void cassetteRoundTripsAndIsByteIdentical() {
    Cassette original = ModelFixtures.cassette();
    byte[] first = cassettes.toBytes(original);
    Cassette loaded = cassettes.read(first);
    byte[] second = cassettes.toBytes(loaded);

    assertThat(second).isEqualTo(first);
    assertThat(cassettes.read(second)).isEqualTo(loaded);
    assertThat(loaded.caseId()).isEqualTo(original.caseId());
    assertThat(loaded.runs()).hasSize(2);
    assertThat(loaded.runs().get(0).usage().estimatedCostUsd()).isNull();
    assertThat(loaded.runs().get(0).steps()).hasSize(2);
    String text = new String(first, StandardCharsets.UTF_8);
    assertThat(text).endsWith("\n");
    assertThat(text).contains("\"schemaVersion\" : \"1.0\"");
    assertThat(text.indexOf("\"a\" : \"keep\"")).isLessThan(text.indexOf("\"z\" : 1"));
    assertThat(text).contains("\"recordedAt\" : \"2026-11-12T02:15:00.123Z\"");
    assertThat(text).contains("\"estimatedCostUsd\" : null");
    assertThat(text).contains("\"type\" : \"llm\"");
    assertThat(text).contains("\"type\" : \"tool\"");
    assertThat(text.indexOf("\"type\" : \"llm\""))
        .isLessThan(text.lastIndexOf("\"type\" : \"tool\""));
  }

  @Test
  void baselineSortsCasesAndRoundTrips() {
    Baseline original = ModelFixtures.baseline();
    byte[] bytes = baselines.toBytes(original);
    Baseline loaded = baselines.read(bytes);

    assertThat(loaded).isEqualTo(original);
    assertThat(baselines.toBytes(loaded)).isEqualTo(bytes);
    assertThat(loaded.cases()).extracting(CaseBaseline::caseId).containsExactly("alpha", "zeta");
    assertThat(loaded.allowRegression()).isFalse();
    assertThat(loaded.gateRules())
        .extracting(GateRule::metric)
        .contains("pass_rate", "judge_score", "steps");
  }

  @Test
  void evalCaseResultAndReportRoundTrip() {
    assertRoundTrip(ModelFixtures.evalCase(), EvalCase.class);
    assertRoundTrip(ModelFixtures.evalResult(), EvalResult.class);
    assertRoundTrip(ModelFixtures.report(), Report.class);
  }

  @Test
  void priceTableRoundTrips() {
    PriceTable original = ModelFixtures.priceTable();
    byte[] bytes = PriceTables.write(original);
    assertThat(PriceTables.read(bytes)).isEqualTo(original);
    assertThat(PriceTables.write(PriceTables.read(bytes))).isEqualTo(bytes);
  }

  @Test
  void rejectedCaseIdsAndSchemaVersions() {
    assertThatThrownBy(
            () ->
                new Cassette(
                    SchemaVersions.CURRENT,
                    "Bad Id",
                    "support",
                    ModelFixtures.WHEN,
                    "0.1.0",
                    new AgentRef("a", "b"),
                    Integration.HTTP,
                    RedactionRecord.none(),
                    java.util.List.of()))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () ->
                new Cassette(
                    "2.0",
                    "ok",
                    "support",
                    ModelFixtures.WHEN,
                    "0.1.0",
                    new AgentRef("a", "b"),
                    Integration.HTTP,
                    RedactionRecord.none(),
                    java.util.List.of()))
        .isInstanceOf(io.github.guptagautam768.aeh.schema.UnsupportedSchemaException.class);
  }

  private <T extends VersionedDocument> void assertRoundTrip(T value, Class<T> type) {
    byte[] first = documents.write(value);
    T loaded = documents.read(first, type);
    assertThat(loaded).isEqualTo(value);
    assertThat(documents.write(loaded)).isEqualTo(first);
  }
}
