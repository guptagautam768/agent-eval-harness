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

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.github.guptagautam768.aeh.json.Decimals;
import io.github.guptagautam768.aeh.json.VersionedDocument;
import io.github.guptagautam768.aeh.schema.SchemaVersions;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Committed per-suite metrics (DM-007, ADR-0008). {@code allowRegression} is recorded when promote
 * was forced past a MUST violation.
 */
@JsonPropertyOrder({
  "schemaVersion",
  "suite",
  "createdAt",
  "harnessVersion",
  "configFingerprint",
  "cases",
  "aggregates",
  "gateRules",
  "allowRegression"
})
public record Baseline(
    String schemaVersion,
    String suite,
    Instant createdAt,
    String harnessVersion,
    String configFingerprint,
    List<CaseBaseline> cases,
    Map<String, BigDecimal> aggregates,
    List<GateRule> gateRules,
    boolean allowRegression)
    implements VersionedDocument {

  public Baseline {
    SchemaVersions.requireSupported(schemaVersion, "baseline");
    suite = Checks.required(suite, "suite");
    createdAt = createdAt == null ? null : createdAt.truncatedTo(ChronoUnit.MILLIS);
    if (createdAt == null) {
      throw new IllegalArgumentException("createdAt is required");
    }
    harnessVersion = Checks.required(harnessVersion, "harnessVersion");
    configFingerprint = Checks.required(configFingerprint, "configFingerprint");
    List<CaseBaseline> ordered =
        new ArrayList<>(Checks.list(cases == null ? List.of() : cases, "cases"));
    ordered.sort(Comparator.comparing(CaseBaseline::caseId));
    cases = List.copyOf(ordered);
    aggregates = Decimals.canonicalMap(aggregates == null ? Map.of() : aggregates);
    gateRules = Checks.list(gateRules == null ? List.of() : gateRules, "gateRules");
  }
}
