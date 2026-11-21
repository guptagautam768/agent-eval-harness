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
import com.fasterxml.jackson.databind.JsonNode;
import io.github.guptagautam768.aeh.json.Decimals;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Sampling parameters that are part of the request hash input (ADR-0004 names {@code stream},
 * {@code seed} and {@code user} as normalization inputs). Anything else is kept under {@code
 * additional}.
 */
@JsonPropertyOrder({"temperature", "maxTokens", "stream", "seed", "user", "additional"})
public record LlmParams(
    BigDecimal temperature,
    Integer maxTokens,
    Boolean stream,
    Integer seed,
    String user,
    Map<String, JsonNode> additional) {

  public LlmParams {
    temperature = Decimals.canonical(temperature);
    if (maxTokens != null && maxTokens < 0) {
      throw new IllegalArgumentException("maxTokens must be >= 0");
    }
    additional = additional == null ? Map.of() : Map.copyOf(additional);
  }
}
