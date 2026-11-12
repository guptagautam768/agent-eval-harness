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

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.github.guptagautam768.aeh.json.Decimals;
import java.math.BigDecimal;

/** USD per 1,000 tokens. Estimates, not invoices (NG-007). */
@JsonPropertyOrder({"inputPer1kUsd", "outputPer1kUsd"})
public record ModelPrice(BigDecimal inputPer1kUsd, BigDecimal outputPer1kUsd) {

  public ModelPrice {
    inputPer1kUsd = Decimals.canonical(inputPer1kUsd);
    outputPer1kUsd = Decimals.canonical(outputPer1kUsd);
    if (inputPer1kUsd == null || outputPer1kUsd == null) {
      throw new IllegalArgumentException("input and output prices are required");
    }
    if (inputPer1kUsd.signum() < 0 || outputPer1kUsd.signum() < 0) {
      throw new IllegalArgumentException("prices must be >= 0");
    }
  }
}
