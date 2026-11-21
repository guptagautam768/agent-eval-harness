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

/** Per-case budget ceilings (FR-025). Null fields are not enforced. */
@JsonPropertyOrder({"maxTokens", "maxCostUsd", "maxLatencyMs", "maxSteps"})
public record Budgets(
    Integer maxTokens, BigDecimal maxCostUsd, Long maxLatencyMs, Integer maxSteps) {

  public Budgets {
    maxCostUsd = Decimals.canonical(maxCostUsd);
    if (maxTokens != null && maxTokens < 0) {
      throw new IllegalArgumentException("maxTokens must be >= 0");
    }
    if (maxLatencyMs != null && maxLatencyMs < 0) {
      throw new IllegalArgumentException("maxLatencyMs must be >= 0");
    }
    if (maxSteps != null && maxSteps < 0) {
      throw new IllegalArgumentException("maxSteps must be >= 0");
    }
  }
}
