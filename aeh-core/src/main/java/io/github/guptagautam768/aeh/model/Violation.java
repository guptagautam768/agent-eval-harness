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
import java.math.BigDecimal;

/** One gate violation. {@code caseId} is null for a suite-aggregate rule (ADR-0008). */
@JsonPropertyOrder({"caseId", "metric", "baseline", "candidate", "tolerance", "severity"})
public record Violation(
    String caseId,
    String metric,
    BigDecimal baseline,
    BigDecimal candidate,
    String tolerance,
    Severity severity) {

  public Violation {
    metric = Checks.required(metric, "violation.metric");
    if (severity == null) {
      throw new IllegalArgumentException("violation.severity is required");
    }
    baseline = Decimals.canonical(baseline);
    candidate = Decimals.canonical(candidate);
  }
}
