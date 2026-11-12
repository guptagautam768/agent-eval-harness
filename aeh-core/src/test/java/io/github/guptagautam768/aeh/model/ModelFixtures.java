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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import io.github.guptagautam768.aeh.HarnessVersion;
import io.github.guptagautam768.aeh.pricing.ModelPrice;
import io.github.guptagautam768.aeh.pricing.PriceTable;
import io.github.guptagautam768.aeh.schema.SchemaVersions;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class ModelFixtures {

  public static final Instant WHEN = Instant.parse("2026-11-12T02:15:00.123Z");
  private static final JsonNodeFactory NODES = JsonNodeFactory.instance;

  private ModelFixtures() {}

  public static Cassette cassette() {
    return new Cassette(
        SchemaVersions.CURRENT,
        "refund.lookup",
        "support",
        WHEN,
        HarnessVersion.current(),
        new AgentRef("support-agent", "abc123"),
        Integration.SPRING_AI,
        RedactionRecord.none(),
        List.of(okRun(), errorRun()));
  }

  public static Run okRun() {
    JsonNode arguments = NODES.objectNode().put("a", "keep").put("z", 1);
    return new Run(
        "run-1",
        new RunInput(
            List.of(
                new ChatMessage(
                    "user", NODES.textNode("where is order 17?"), null, null, List.of())),
            Map.of("orderId", NODES.textNode("17"))),
        new RunConfig(
            "llama3.1:8b", "ollama", new BigDecimal("0.0"), "support", "3", "schema-hash"),
        List.of(llmCall(arguments), toolCall(arguments)),
        "Order 17 is in transit.",
        new Usage(12, 4, null, "builtin-2026.11"),
        80L,
        RunStatus.OK,
        null);
  }

  public static Run errorRun() {
    return new Run(
        "run-2",
        new RunInput(List.of(), Map.of()),
        new RunConfig("llama3.1:8b", "ollama", null, null, null, null),
        List.of(),
        null,
        new Usage(null, null, null, null),
        5L,
        RunStatus.TIMEOUT,
        "timed out");
  }

  public static LlmCall llmCall(JsonNode arguments) {
    return new LlmCall(
        "llm",
        0,
        "abc",
        "ollama",
        "llama3.1:8b",
        new LlmParams(
            new BigDecimal("0.10"),
            256,
            false,
            null,
            null,
            Map.of("top_p", NODES.numberNode(new BigDecimal("1.0")))),
        Map.of("X-Trace", "ok"),
        List.of(
            new ChatMessage("user", NODES.textNode("where is order 17?"), null, null, List.of())),
        List.of(new ToolSchema("lookupOrder", NODES.objectNode().put("type", "object"))),
        new LlmResponse(
            List.of(
                new ChatMessage(
                    "assistant",
                    NODES.textNode(""),
                    null,
                    null,
                    List.of(new RequestedToolCall("call-1", "lookupOrder", arguments)))),
            List.of(new RequestedToolCall("call-1", "lookupOrder", arguments)),
            "tool_calls"),
        new Usage(12, 4, new BigDecimal("0"), "builtin-2026.11"),
        40L,
        new GatewayMeta(false, "local", "usage-1"),
        List.of(new StreamChunk(0, "Order", null), new StreamChunk(1, " 17", "stop")),
        null);
  }

  public static ToolCall toolCall(JsonNode arguments) {
    return new ToolCall(
        "tool",
        1,
        0,
        "lookupOrder",
        arguments,
        NODES.objectNode().put("status", "transit"),
        3L,
        null);
  }

  public static EvalCase evalCase() {
    return new EvalCase(
        SchemaVersions.CURRENT,
        "refund.lookup",
        List.of("smoke"),
        new RunInput(
            List.of(
                new ChatMessage(
                    "user", NODES.textNode("where is order 17?"), null, null, List.of())),
            Map.of()),
        new Expectations(
            List.of(
                new DeterministicCheck(
                    "mentions-transit", CheckKind.CONTAINS, NODES.textNode("transit"))),
            new ToolCallExpectation(
                List.of("lookupOrder"),
                List.of("escalateToHuman"),
                1,
                2,
                OrderMode.STRICT,
                ArgumentMatch.EXACT,
                List.of(
                    new ExpectedCall(
                        "lookupOrder",
                        NODES.objectNode().put("orderId", "17"),
                        ArgumentMatch.EXACT))),
            List.of(new RubricRef("support-answer-quality", "1")),
            new Budgets(2000, new BigDecimal("0.05"), 10_000L, 6)),
        1);
  }

  public static EvalResult evalResult() {
    return new EvalResult(
        SchemaVersions.CURRENT,
        "refund.lookup",
        List.of("run-1"),
        Map.of("pass_rate", new BigDecimal("1"), "tool_call_score", new BigDecimal("1.0")),
        List.of(new CheckResult("mentions-transit", true, "found", Severity.MUST)),
        List.of(
            new JudgeResult(
                "support-answer-quality",
                "1",
                "qwen2.5:7b-instruct",
                new BigDecimal("0.75"),
                "clear",
                "cache-key")));
  }

  public static Baseline baseline() {
    return new Baseline(
        SchemaVersions.CURRENT,
        "support",
        WHEN,
        HarnessVersion.current(),
        "fingerprint",
        List.of(
            new CaseBaseline("zeta", Map.of("pass_rate", new BigDecimal("1")), true),
            new CaseBaseline("alpha", Map.of("pass_rate", new BigDecimal("0.5")), false)),
        Map.of("pass_rate", new BigDecimal("0.75"), "cost_usd", new BigDecimal("0")),
        GateRules.defaults(RunMode.REPLAY),
        false);
  }

  public static Report report() {
    return new Report(
        SchemaVersions.CURRENT,
        new ReportSummary(1, 1, 0, 0, Map.of("pass_rate", new BigDecimal("1")), true),
        List.of(evalResult()),
        new GateOutcome(true, List.of()),
        List.of("judge score is warn-only"),
        new EnvironmentInfo("21.0.13", "Linux", HarnessVersion.current(), "abc123"),
        "fingerprint");
  }

  public static PriceTable priceTable() {
    return new PriceTable(
        SchemaVersions.CURRENT,
        "test-2026.11",
        Map.of("demo-model", new ModelPrice(new BigDecimal("0.002"), new BigDecimal("0.004"))));
  }
}
