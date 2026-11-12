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

/** One sample of a case (DM-002). Steps are a single ordered list of tagged calls. */
@JsonPropertyOrder({
  "runId",
  "input",
  "config",
  "steps",
  "finalOutput",
  "usage",
  "latencyMs",
  "status",
  "error"
})
public record Run(
    String runId,
    RunInput input,
    RunConfig config,
    List<Step> steps,
    String finalOutput,
    Usage usage,
    long latencyMs,
    RunStatus status,
    String error) {

  public Run {
    runId = Checks.required(runId, "runId");
    if (input == null || config == null || status == null) {
      throw new IllegalArgumentException("input, config and status are required");
    }
    if (latencyMs < 0) {
      throw new IllegalArgumentException("latencyMs must be >= 0");
    }
    steps = Checks.list(steps == null ? List.of() : steps, "steps");
  }
}
