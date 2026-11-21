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
package io.github.guptagautam768.aeh.json;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Canonical plain-decimal form so rewritten files stay byte-identical (ADR-0003). */
public final class Decimals {

  private Decimals() {}

  public static BigDecimal canonical(BigDecimal value) {
    if (value == null) {
      return null;
    }
    BigDecimal stripped = value.stripTrailingZeros();
    if (stripped.scale() < 0) {
      stripped = stripped.setScale(0);
    }
    return stripped;
  }

  public static String plain(BigDecimal value) {
    return canonical(value).toPlainString();
  }

  public static Map<String, BigDecimal> canonicalMap(Map<String, BigDecimal> values) {
    Objects.requireNonNull(values, "values");
    Map<String, BigDecimal> copy = new LinkedHashMap<>();
    values.forEach(
        (key, value) -> {
          if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("metric name is required");
          }
          copy.put(key, canonical(Objects.requireNonNull(value, key)));
        });
    return Map.copyOf(copy);
  }
}
