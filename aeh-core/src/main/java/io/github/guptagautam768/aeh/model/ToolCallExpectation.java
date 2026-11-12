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
import java.util.List;

@JsonPropertyOrder({
  "expectedTools",
  "forbiddenTools",
  "minCalls",
  "maxCalls",
  "orderMode",
  "argumentMatch",
  "calls"
})
public record ToolCallExpectation(
    List<String> expectedTools,
    List<String> forbiddenTools,
    Integer minCalls,
    Integer maxCalls,
    OrderMode orderMode,
    ArgumentMatch argumentMatch,
    List<ExpectedCall> calls) {

  public ToolCallExpectation {
    expectedTools = Checks.list(expectedTools == null ? List.of() : expectedTools, "expectedTools");
    forbiddenTools =
        Checks.list(forbiddenTools == null ? List.of() : forbiddenTools, "forbiddenTools");
    calls = Checks.list(calls == null ? List.of() : calls, "calls");
    if (minCalls != null && minCalls < 0) {
      throw new IllegalArgumentException("minCalls must be >= 0");
    }
    if (maxCalls != null && maxCalls < 0) {
      throw new IllegalArgumentException("maxCalls must be >= 0");
    }
  }
}
