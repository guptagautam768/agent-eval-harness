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

import java.math.BigDecimal;

/**
 * {@code costUsd} is null when the model is missing from the table or token counts are incomplete.
 * {@code warning} is safe to log: it names the model, never prompt text.
 */
public record CostEstimate(BigDecimal costUsd, String priceTableVersion, String warning) {

  public boolean priced() {
    return costUsd != null && warning == null;
  }
}
