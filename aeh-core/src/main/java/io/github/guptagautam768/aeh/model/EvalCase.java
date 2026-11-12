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

/** Case definition: tags, input, expectations and sample count (DM-005). */
@JsonPropertyOrder({"schemaVersion", "caseId", "tags", "input", "expectations", "samples"})
public record EvalCase(
    String schemaVersion,
    String caseId,
    List<String> tags,
    RunInput input,
    Expectations expectations,
    int samples)
    implements VersionedDocument {

  public EvalCase {
    SchemaVersions.requireSupported(schemaVersion, "eval case");
    Checks.caseId(caseId);
    tags = Checks.list(tags == null ? List.of() : tags, "tags");
    if (input == null) {
      throw new IllegalArgumentException("input is required");
    }
    if (samples < 1) {
      throw new IllegalArgumentException("samples must be >= 1");
    }
  }
}
