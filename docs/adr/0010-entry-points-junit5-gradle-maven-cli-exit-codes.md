# ADR-0010: Entry points: JUnit 5 extension, Gradle and Maven plugins, `aeh` CLI, and exit codes

| Field | Value |
|---|---|
| **Status** | Accepted (12 Nov 2026, Gautam Gupta) |
| **Date** | 12 Nov 2026 IST |
| **Deciders** | Gautam Gupta |
| **Requirements** | [SDR-0001 v1.0](../sdr/SDR-0001-agent-eval-harness.md) |

## Context

G-006 and FR-040–046 require a JUnit 5 extension, a Gradle plugin, a Maven plugin (RD-008: in v1.0 as a thin wrapper; may slip to v1.1), and a CLI with documented exit codes. AC-010 requires the same gate result from every entry point. CI-001–004 describe PR and nightly jobs.

## Decision drivers

- G-006, FR-040…FR-046, RD-008, AC-010, CI-001…CI-004, R-007.

## Considered options

1. **One engine API (`EvalRunner`) that every entry point calls; plugins shell out to nothing.**
2. Plugins that spawn the CLI as a subprocess.
3. JUnit-only, with Gradle/Maven running tests normally.

## Decision outcome

**Chosen: option 1.** `aeh-engine` exposes `EvalRunner.run(SuiteConfig) → SuiteResult` and `Gate.evaluate(SuiteResult, Baseline, Rules) → GateResult`; every entry point is an adapter.

- **JUnit 5 (`aeh-junit5`):** `@AgentEvalSuite(name, mode)` on a test class; `@AgentEval(caseId, tags)` on methods (or `@AgentEvalCases` for a directory-driven `@TestFactory`); `@Rubric`, `@ExpectTools`, `@Budget` for declarative expectations, plus a fluent `EvalAssertions` API. Each case is a test; Must check failures fail that test. Suite gate is evaluated in `AfterAllCallback`; violations fail the class as a container failure with the violation list. Reports written at the end of the suite.
- **Gradle plugin (`io.github.guptagautam768.agent-eval`):** adds an `agentEval` source set (`src/agentEval/java`) and a `Test` task `agentEval` so evals don't slow unit tests; tasks `agentEvalCompare`, `agentEvalPromoteBaseline`, `agentEvalReport`, `agentEvalLint`. `check` depends on `agentEvalLint` always and on `agentEval` when `agentEval.runOnCheck = true` (default `true`). Configuration cache compatible; tested with Gradle TestKit.
- **Maven plugin (`aeh-maven-plugin`, RD-008):** goals `aeh:eval` (binds to `integration-test`; runs the engine on a configured test classpath via Surefire-style tags), `aeh:compare`, `aeh:promote-baseline`, `aeh:report`, `aeh:lint` (binds to `verify`). Descriptor generated in the Gradle build. If week 5 overruns it ships in v1.1 and the README documents CLI usage for Maven.
- **CLI (`aeh-cli`, picocli):** `aeh run | compare | report | baseline promote | lint | migrate`. Distributed as a runnable `-all` jar on GitHub Releases and Maven Central; no native image in v1. The CLI scores existing cassettes with deterministic, tool-call, budget and cached-judge scorers; running an agent live from the CLI requires a user-provided `AgentEntrypoint` class on the classpath.
- **Exit codes:** `0` pass (warnings allowed); `1` gate failed (MUST violation); `2` configuration / usage error (incl. `record` in CI); `3` cassette integrity or redaction violation (lint hit, schema invalid); **`4` run aborted (live-run cost ceiling hit or unrecoverable provider error)**. Gradle/Maven map 1/3/4 to build failures with the same message.
- **GitHub Actions:** `examples/.github/workflows/agent-eval.yml` sample runs `./gradlew agentEvalLint agentEval`, uploads `build/reports/aeh/` as an artifact, and appends `summary.md` to `$GITHUB_STEP_SUMMARY`. A PR-comment step is documented as optional (FR-046).

## Consequences

- ➕ One engine guarantees identical results across entry points (AC-010 is testable).
- ➕ Separate source set keeps eval suites out of fast unit-test loops.
- ➖ Three adapters to maintain; Maven plugin is the first to slip if time is short.
- ➖ Exit code 4 is an addition beyond the SDR's 0–3 list (see review checklist).

## Links

- [SDR-0001](../sdr/SDR-0001-agent-eval-harness.md)
- [ADR index](./README.md)
