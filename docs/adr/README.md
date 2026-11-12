# Architecture Decision Records — `agent-eval-harness`

Decisions on *how* to build what [SDR-0001 v1.0 (APPROVED)](../sdr/SDR-0001-agent-eval-harness.md) requires. Template and lifecycle: [ADR-0001](./0001-record-architecture-decisions.md). Review: [REVIEW.md](./REVIEW.md).

| ADR | Title | Status |
|---|---|---|
| [0001](./0001-record-architecture-decisions.md) | Record architecture decisions | Accepted |
| [0002](./0002-build-packaging-java21-toolchain-gradle-maven-central.md) | Build and packaging: Java 21 toolchain targeting Java 17, Gradle multi-module, Maven Central + Gradle Plugin Portal | Accepted |
| [0003](./0003-cassette-format-and-storage.md) | Cassette format and storage: pretty-printed JSON, one file per case, committed to the user's repo | Accepted |
| [0004](./0004-run-modes-and-request-matching.md) | Run modes (record / replay / rerun / live) and request matching | Accepted |
| [0005](./0005-integration-spi-and-adapter-priority.md) | Integration SPI and adapter priority: Spring AI + OpenAI-compatible HTTP first | Accepted |
| [0006](./0006-scoring-pipeline-and-gate-severities.md) | Scoring pipeline and gate severities: layered scorers, Must gates fail, Should gates warn | Accepted |
| [0007](./0007-llm-judge-design-caching-default-model.md) | LLM-as-judge: anchored rubrics, temperature 0, content-hash cache committed to git, local Ollama default | Accepted |
| [0008](./0008-baselines-tolerances-promote-workflow.md) | Baselines, tolerances and the promote workflow | Accepted |
| [0009](./0009-redaction-of-secrets-and-pii.md) | Redaction: in-memory, before write; typed stable placeholders; redact-and-continue default; lint gate | Accepted |
| [0010](./0010-entry-points-junit5-gradle-maven-cli-exit-codes.md) | Entry points: JUnit 5 extension, Gradle and Maven plugins, `aeh` CLI, and exit codes | Accepted |
| [0011](./0011-reports-json-html-junit-xml.md) | Reports: versioned JSON schema, single-file offline HTML, JUnit XML and Markdown summary | Accepted |
| [0012](./0012-testing-ci-and-licensing.md) | Testing, CI and licensing: JUnit 5, WireMock, Testcontainers (Ollama), Gradle TestKit, GitHub Actions, CodeQL, Apache-2.0 | Accepted |
| [0013](./0013-demo-agent-customer-support-example.md) | Demo agent: Spring AI customer-support agent over fake in-memory data | Accepted |

**Process:** Proposed → reviewed by Gautam Gupta → Accepted / Rejected. No implementation before acceptance. Superseding decisions get a new number.
