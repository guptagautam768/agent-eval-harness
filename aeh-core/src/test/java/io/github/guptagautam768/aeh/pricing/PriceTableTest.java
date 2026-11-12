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

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class PriceTableTest {

  @Test
  void builtinTablePricesPinnedLocalModelsAtZero() {
    PriceTable table = PriceTables.builtin();
    CostEstimator estimator = new CostEstimator(table);

    CostEstimate llama = estimator.estimate("llama3.1:8b", 1000, 500);
    assertThat(llama.priced()).isTrue();
    assertThat(llama.costUsd()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(llama.priceTableVersion()).isEqualTo("builtin-2026.11");
    assertThat(llama.warning()).isNull();

    CostEstimate judge = estimator.estimate("qwen2.5:7b-instruct", 0, 0);
    assertThat(judge.costUsd()).isEqualByComparingTo(BigDecimal.ZERO);
  }

  @Test
  void missingPriceAndIncompleteTokensLeaveCostNull() {
    CostEstimator estimator = new CostEstimator(PriceTables.builtin());

    CostEstimate missing = estimator.estimate("gpt-unknown", 10, 10);
    assertThat(missing.costUsd()).isNull();
    assertThat(missing.warning()).contains("gpt-unknown").doesNotContain("prompt");

    CostEstimate incomplete = estimator.estimate("llama3.1:8b", null, 4);
    assertThat(incomplete.costUsd()).isNull();
    assertThat(incomplete.warning()).contains("token usage incomplete");
  }

  @Test
  void knownPriceIsInputPlusOutputPerThousandTokens() {
    CostEstimator estimator =
        new CostEstimator(io.github.guptagautam768.aeh.model.ModelFixtures.priceTable());
    CostEstimate estimate = estimator.estimate("demo-model", 1000, 500);

    assertThat(estimate.priced()).isTrue();
    assertThat(estimate.costUsd()).isEqualByComparingTo(new BigDecimal("0.004"));
    assertThat(estimate.priceTableVersion()).isEqualTo("test-2026.11");
  }
}
