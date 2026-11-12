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
import java.util.Map;

@JsonPropertyOrder({"caseCount", "passed", "failed", "aborted", "aggregates", "gatePass"})
public record ReportSummary(
    int caseCount,
    int passed,
    int failed,
    int aborted,
    Map<String, BigDecimal> aggregates,
    boolean gatePass) {

  public ReportSummary {
    if (caseCount < 0 || passed < 0 || failed < 0 || aborted < 0) {
      throw new IllegalArgumentException("summary counts must be >= 0");
    }
    aggregates = Decimals.canonicalMap(aggregates == null ? Map.of() : aggregates);
  }
}
