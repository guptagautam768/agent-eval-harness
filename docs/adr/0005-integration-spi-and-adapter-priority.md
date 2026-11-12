# ADR-0005: Integration SPI and adapter priority: Spring AI + OpenAI-compatible HTTP first

| Field | Value |
|---|---|
| **Status** | Accepted (12 Nov 2026, Gautam Gupta) |
| **Date** | 12 Nov 2026 IST |
| **Deciders** | Gautam Gupta |
| **Requirements** | [SDR-0001 v1.0](../sdr/SDR-0001-agent-eval-harness.md) |

## Context

FR-004 requires capture via framework hooks and via an HTTP-level path. RD-006 fixes priority: **Must** Spring AI + raw OpenAI-compatible HTTP (which also covers Ollama and jvm-llm-gateway); **Should** LangChain4j (week 5) and Anthropic native (week 6); **Could** jvm-llm-gateway metadata. R-001 warns about framework API churn.

## Decision drivers

- RD-006, FR-004, FR-050…FR-054, NFR-006, R-001, AC-001, AC-012.

## Considered options

1. **One small SPI in `aeh-core` + thin adapters**, with an embedded HTTP record/replay proxy as the universal fallback.
2. HTTP proxy only (no framework adapters).
3. Framework adapters only.

## Decision outcome

**Chosen: option 1.**

- **SPI (`aeh-core`):** `AgentTransport` with two seams the engine controls: `LlmExchange intercept(LlmRequest, Function<LlmRequest, LlmResponse> next)` and `ToolExchange interceptTool(ToolInvocation, Function<ToolInvocation, ToolResult> next)`. The engine decides per mode whether `next` is called (live) or a recorded value is returned (replay). Adapters map framework types to the neutral `LlmRequest/LlmResponse/ToolInvocation` model (DM-003/004). Discovery via `ServiceLoader`.
- **`aeh-http` (Must, week 2):** an embedded, in-process **record/replay proxy** on `127.0.0.1:<random port>` built on the JDK `HttpServer` (upstream calls via JDK `HttpClient`), speaking the OpenAI Chat Completions shape including SSE. Users point their client's base URL at it (the JUnit extension injects it). Works for any OpenAI-compatible client, Ollama's OpenAI endpoint, and jvm-llm-gateway. Tool calls are visible only as `tool_calls` in LLM messages here; tool *execution* capture needs a framework adapter or the `@AgentTool` wrapper helper.
- **`aeh-spring-ai` (Must, week 2):** a Spring AI `CallAdvisor`/`StreamAdvisor` for LLM exchanges and a decorator around `ToolCallback`s for tool executions; Spring Boot test auto-configuration activated by `@AgentEvalSuite`. Tested against the Spring AI 1.x line pinned in the version catalog.
- **`aeh-langchain4j` (Should, week 5):** `ChatModelListener` for capture plus a replaying `ChatModel` wrapper and tool-executor decorator (listeners alone cannot short-circuit calls).
- **Anthropic native (Should, week 6):** a second wire shape in `aeh-http` for the Messages API (`/v1/messages`), including tool-use blocks.
- **jvm-llm-gateway metadata (Could, week 6):** `aeh-http` reads gateway response headers / usage fields when present into `gatewayMeta`; no compile-time dependency on the gateway.

## Consequences

- ➕ The HTTP proxy gives day-one coverage of many clients with no framework coupling, and it isolates us from framework churn.
- ➕ Framework adapters capture tool executions precisely, which tool-call scoring needs.
- ➖ The proxy needs a base-URL change in the agent's config; agents with hard-coded endpoints can't use it.
- ➖ JDK `HttpServer` is basic; fine for a local test proxy, not for load. Revisit if SSE edge cases bite.
- ➖ Two Spring AI seams (advisor + tool decorator) must stay in sync with Spring AI releases; CI pins and tests the supported line.

## Links

- [SDR-0001](../sdr/SDR-0001-agent-eval-harness.md)
- [ADR index](./README.md)
