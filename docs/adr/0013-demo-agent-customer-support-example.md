# ADR-0013: Demo agent: Spring AI customer-support agent over fake in-memory data

| Field | Value |
|---|---|
| **Status** | Accepted (12 Nov 2026, Gautam Gupta) |
| **Date** | 12 Nov 2026 IST |
| **Deciders** | Gautam Gupta |
| **Requirements** | [SDR-0001 v1.0](../sdr/SDR-0001-agent-eval-harness.md) |

## Context

RD-014 fixes the demo domain: a customer-support agent with 3–4 tools (order lookup, refund policy, ticket creation) over fake in-memory data, chosen to show tool-call correctness and avoid overlap with resume-jd-matcher. G-010 requires a zero-cost local path; AC-001 a committed sample cassette; NFR-010 a 15-minute quickstart.

## Decision drivers

- RD-014, G-010, G-011, AC-001, AC-002, AC-015, NFR-010, ADR-0005, ADR-0007.

## Considered options

1. **Spring Boot + Spring AI `ChatClient` agent with four `@Tool` methods, Ollama by default.**
2. Plain Java agent on the HTTP proxy only.
3. LangChain4j agent as the primary example.

## Decision outcome

**Chosen: option 1**, in `examples/spring-ai-support-agent` (Java 21, not published).

- **Tools:** `lookupOrder(orderId)`, `getRefundPolicy(category)`, `createTicket(orderId, reason, priority)`, and `escalateToHuman(reason)`, used to test *forbidden/unnecessary tool* checks. Data: a fixed in-memory catalogue of fictional orders and customers (synthetic emails/phones so redaction is visible in cassettes).
- **Model:** Ollama `llama3.1:8b` by default (ADR-0007); any OpenAI-compatible endpoint via env.
- **Eval suite:** a starter set of about a dozen cases across: happy-path lookups, refund eligibility, ticket creation with correct arguments, a case where escalation must *not* happen, a prompt-injection-style case inside a tool result, and a budget case. Mix of deterministic, tool-call and judge (rubric `support-answer-quality`) scoring.
- **Committed artefacts:** cassettes, baseline, judge cache (RD-010), and the sample GitHub Actions workflow.
- **Demo script:** `scripts/demo.sh` that (1) runs replay → green; (2) applies a deliberately bad prompt change and runs rerun (needs Ollama) → gate fails with a readable violation; (3) opens the HTML report. A recorded GIF is added in week 6.
- A second, minimal LangChain4j example is added in week 5 to prove AC-012, not as a full demo.

## Consequences

- ➕ Tool calls are central to the domain, so tool-call scoring has something real to show.
- ➕ Runs fully locally; no paid keys.
- ➖ An 8B local model makes mistakes; good for showing regressions, but the baseline will not look "perfect" and the README must say the scores reflect a small local model.
- ➖ Rerun demo step needs Ollama installed or Docker.

## Links

- [SDR-0001](../sdr/SDR-0001-agent-eval-harness.md)
- [ADR index](./README.md)
