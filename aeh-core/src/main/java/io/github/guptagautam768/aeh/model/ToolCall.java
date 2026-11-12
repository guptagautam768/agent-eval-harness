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

/** One tool execution linked to the model call that requested it (DM-004). */
@JsonPropertyOrder({
  "type",
  "seq",
  "parentLlmSeq",
  "toolName",
  "arguments",
  "result",
  "latencyMs",
  "error"
})
public record ToolCall(
    String type,
    int seq,
    int parentLlmSeq,
    String toolName,
    JsonNode arguments,
    JsonNode result,
    long latencyMs,
    String error)
    implements Step {

  public ToolCall {
    if (!"tool".equals(type)) {
      throw new IllegalArgumentException("ToolCall.type must be 'tool'");
    }
    if (seq < 0 || parentLlmSeq < 0) {
      throw new IllegalArgumentException("seq and parentLlmSeq must be >= 0");
    }
    if (latencyMs < 0) {
      throw new IllegalArgumentException("latencyMs must be >= 0");
    }
    toolName = Checks.required(toolName, "toolName");
  }
}
