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
import java.util.List;

/**
 * One message. {@code content} is a string or an array of parts so multimodal messages fit without
 * a schema break. {@code toolCalls} is empty when the message did not request tools.
 */
@JsonPropertyOrder({"role", "content", "name", "toolCallId", "toolCalls"})
public record ChatMessage(
    String role,
    JsonNode content,
    String name,
    String toolCallId,
    List<RequestedToolCall> toolCalls) {

  public ChatMessage {
    role = Checks.required(role, "message.role");
    toolCalls = Checks.list(toolCalls == null ? List.of() : toolCalls, "message.toolCalls");
  }
}
