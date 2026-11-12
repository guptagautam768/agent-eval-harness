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
import java.util.Map;

/** One model call (DM-003). {@code headers} holds non-secret headers after redaction (SEC-002). */
@JsonPropertyOrder({
  "type",
  "seq",
  "requestHash",
  "provider",
  "model",
  "params",
  "headers",
  "messages",
  "toolSchemas",
  "response",
  "usage",
  "latencyMs",
  "gatewayMeta",
  "chunks",
  "error"
})
public record LlmCall(
    String type,
    int seq,
    String requestHash,
    String provider,
    String model,
    LlmParams params,
    Map<String, String> headers,
    List<ChatMessage> messages,
    List<ToolSchema> toolSchemas,
    LlmResponse response,
    Usage usage,
    long latencyMs,
    GatewayMeta gatewayMeta,
    List<StreamChunk> chunks,
    String error)
    implements Step {

  public LlmCall {
    if (!"llm".equals(type)) {
      throw new IllegalArgumentException("LlmCall.type must be 'llm'");
    }
    if (seq < 0) {
      throw new IllegalArgumentException("seq must be >= 0");
    }
    if (latencyMs < 0) {
      throw new IllegalArgumentException("latencyMs must be >= 0");
    }
    requestHash = Checks.required(requestHash, "requestHash");
    provider = Checks.required(provider, "provider");
    model = Checks.required(model, "model");
    if (params == null) {
      throw new IllegalArgumentException("params is required");
    }
    headers = Checks.strings(headers == null ? Map.of() : headers, "headers");
    messages = Checks.list(messages == null ? List.of() : messages, "messages");
    toolSchemas = Checks.list(toolSchemas == null ? List.of() : toolSchemas, "toolSchemas");
    if (response == null) {
      throw new IllegalArgumentException("response is required");
    }
    chunks = chunks == null ? null : Checks.list(chunks, "chunks");
  }
}
