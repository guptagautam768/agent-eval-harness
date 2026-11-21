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
package io.github.guptagautam768.aeh.pricing;

import io.github.guptagautam768.aeh.json.Decimals;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/** Estimated USD from a price table (FR-005). Unknown models yield a null cost and a warning. */
public final class CostEstimator {

  private static final BigDecimal THOUSAND = new BigDecimal("1000");
  private static final int SCALE = 8;

  private final PriceTable table;

  public CostEstimator(PriceTable table) {
    this.table = Objects.requireNonNull(table, "table");
  }

  public CostEstimate estimate(String model, Integer inputTokens, Integer outputTokens) {
    if (model == null || model.isBlank()) {
      return new CostEstimate(null, table.version(), "model name is missing; cost left null");
    }
    ModelPrice price = table.priceFor(model);
    if (price == null) {
      return new CostEstimate(
          null, table.version(), "no price for model '" + model + "'; cost left null");
    }
    if (inputTokens == null || outputTokens == null) {
      return new CostEstimate(
          null,
          table.version(),
          "token usage incomplete for model '" + model + "'; cost left null");
    }
    BigDecimal input =
        price
            .inputPer1kUsd()
            .multiply(BigDecimal.valueOf(inputTokens.longValue()))
            .divide(THOUSAND, SCALE, RoundingMode.HALF_EVEN);
    BigDecimal output =
        price
            .outputPer1kUsd()
            .multiply(BigDecimal.valueOf(outputTokens.longValue()))
            .divide(THOUSAND, SCALE, RoundingMode.HALF_EVEN);
    return new CostEstimate(Decimals.canonical(input.add(output)), table.version(), null);
  }
}
