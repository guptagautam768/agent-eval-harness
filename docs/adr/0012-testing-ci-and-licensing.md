# ADR-0012: Testing, CI and licensing: JUnit 5, WireMock, Testcontainers (Ollama), Gradle TestKit, GitHub Actions, CodeQL, Apache-2.0

| Field | Value |
|---|---|
| **Status** | Accepted (12 Nov 2026, Gautam Gupta) |
| **Date** | 12 Nov 2026 IST |
| **Deciders** | Gautam Gupta |
| **Requirements** | [SDR-0001 v1.0](../sdr/SDR-0001-agent-eval-harness.md) |

## Context

NFR-008 and NFR-011 require unit, WireMock, Testcontainers and plugin functional tests, plus CodeQL, dependency review and a license report. CI-005 lists the harness repo's own CI. RD-002 fixes Apache-2.0. RD-004 means tests must also run on a Java 17 runtime. The jvm-llm-gateway release left the license report out of CI; this project should not repeat that gap.

## Decision drivers

- RD-002, RD-004, NFR-008, NFR-011, CI-005, AC-002, AC-014, R-001.

## Considered options

1. **GitHub Actions with a PR workflow (fast, no network models) + nightly workflow (Ollama via Testcontainers)**.
2. Single workflow running everything on every PR.
3. Another CI provider.

## Decision outcome

**Chosen: option 1.**

- **Unit tests:** JUnit 5 + AssertJ for model I/O, matchers, scorers, gate, redactor (with synthetic secrets/PII fixtures only; no real keys ever).
- **Integration:** WireMock as a fake OpenAI/Anthropic endpoint for `aeh-http`, `aeh-judge` and adapters; Spring Boot test slices for `aeh-spring-ai`.
- **Plugin tests:** Gradle TestKit functional tests over sample builds; Maven plugin tested with the Maven Invoker approach (or embedded Maven) in CI.
- **Live path:** Testcontainers `ollama/ollama` module running the pinned models (ADR-0007) in a **nightly / manual** workflow only, to keep PR CI fast and free.
- **PR workflow:** build + tests on Java 21 runtime **and** a Java 17 runtime job for published modules; JaCoCo coverage report; Spotless; example project's eval suite in `replay` with outbound network blocked (AC-002); `agentEvalLint` over all committed cassettes; dependency review; **license report** (`com.github.jk1.dependency-license-report`) failing on disallowed licenses (GPL/AGPL family) for published modules.
- **Security:** CodeQL (Java) on PR and weekly; Dependabot for Gradle and Actions.
- **Framework matrix:** the Spring AI and LangChain4j versions pinned in the catalog; nightly job also tests the latest minor of each to surface churn early (R-001).
- **License:** Apache-2.0 `LICENSE` + `NOTICE`; SPDX headers in source files enforced by Spotless.

## Consequences

- ➕ PR CI needs no paid keys and no model downloads.
- ➕ License report closes the gap left in jvm-llm-gateway.
- ➖ Live regressions with real models surface only nightly.
- ➖ Ollama model pulls in nightly CI are slow and large; cached between runs where Actions cache allows.

## Links

- [SDR-0001](../sdr/SDR-0001-agent-eval-harness.md)
- [ADR index](./README.md)
