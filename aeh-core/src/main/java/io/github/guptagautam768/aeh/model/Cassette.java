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
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/** One eval case's recorded runs (DM-001, ADR-0003). */
@JsonPropertyOrder({
  "schemaVersion",
  "caseId",
  "suite",
  "recordedAt",
  "harnessVersion",
  "agent",
  "integration",
  "redaction",
  "runs"
})
public record Cassette(
    String schemaVersion,
    String caseId,
    String suite,
    Instant recordedAt,
    String harnessVersion,
    AgentRef agent,
    Integration integration,
    RedactionRecord redaction,
    List<Run> runs)
    implements VersionedDocument {

  public Cassette {
    SchemaVersions.requireSupported(schemaVersion, "cassette");
    Checks.caseId(caseId);
    suite = Checks.required(suite, "suite");
    recordedAt = recordedAt == null ? null : recordedAt.truncatedTo(ChronoUnit.MILLIS);
    if (recordedAt == null) {
      throw new IllegalArgumentException("recordedAt is required");
    }
    harnessVersion = Checks.required(harnessVersion, "harnessVersion");
    if (agent == null || integration == null || redaction == null) {
      throw new IllegalArgumentException("agent, integration and redaction are required");
    }
    runs = Checks.list(runs == null ? List.of() : runs, "runs");
  }
}
