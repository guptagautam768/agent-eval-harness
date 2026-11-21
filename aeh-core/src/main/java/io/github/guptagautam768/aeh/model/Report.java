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
import io.github.guptagautam768.aeh.json.VersionedDocument;
import io.github.guptagautam768.aeh.schema.SchemaVersions;
import java.util.List;

/** JSON report document (DM-008, ADR-0011). HTML rendering is week 5. */
@JsonPropertyOrder({
  "schemaVersion",
  "summary",
  "results",
  "gate",
  "warnings",
  "environment",
  "configFingerprint"
})
public record Report(
    String schemaVersion,
    ReportSummary summary,
    List<EvalResult> results,
    GateOutcome gate,
    List<String> warnings,
    EnvironmentInfo environment,
    String configFingerprint)
    implements VersionedDocument {

  public Report {
    SchemaVersions.requireSupported(schemaVersion, "report");
    if (summary == null || gate == null || environment == null) {
      throw new IllegalArgumentException("summary, gate and environment are required");
    }
    results = Checks.list(results == null ? List.of() : results, "results");
    warnings = Checks.list(warnings == null ? List.of() : warnings, "warnings");
    configFingerprint = Checks.required(configFingerprint, "configFingerprint");
  }
}
