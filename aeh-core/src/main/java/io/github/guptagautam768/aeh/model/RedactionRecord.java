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
import java.util.Map;

/** Redaction policy and per-type counts stored on a cassette. Counts never include raw values. */
@JsonPropertyOrder({"policyId", "rulesApplied", "counts"})
public record RedactionRecord(
    String policyId, java.util.List<String> rulesApplied, Map<String, Integer> counts) {

  public RedactionRecord {
    policyId = Checks.required(policyId, "redaction.policyId");
    rulesApplied = Checks.list(rulesApplied, "redaction.rulesApplied");
    counts = Map.copyOf(counts == null ? Map.of() : counts);
  }

  public static RedactionRecord none() {
    return new RedactionRecord("none", java.util.List.of(), Map.of());
  }
}
