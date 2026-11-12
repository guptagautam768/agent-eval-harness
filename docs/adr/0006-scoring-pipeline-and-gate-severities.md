# ADR-0006: Scoring pipeline and gate severities: layered scorers, Must gates fail, Should gates warn

| Field | Value |
|---|---|
| **Status** | Accepted (12 Nov 2026, Gautam Gupta) |
| **Date** | 12 Nov 2026 IST |
| **Deciders** | Gautam Gupta |
| **Requirements** | [SDR-0001 v1.0](../sdr/SDR-0001-agent-eval-harness.md) |

## Context

§7 orders scoring layers (deterministic → tool-call → budget → judge). FR-020–027 define scorers, metrics and a pluggable SPI. RD-007: judge metrics are **Should** (warn) gates by default; deterministic, tool-call and budget metrics are **Must** (fail) gates; users can promote judge gates to Must per metric.

## Decision drivers

- §7, FR-020…FR-027, FR-031, FR-032, RD-007, NFR-002, AC-005, AC-007, R-003.

## Considered options

1. **Ordered pipeline of independent scorers producing named metrics, with a separate gate step assigning severity per metric.**
2. One weighted composite score with a single threshold.
3. Scorers that directly throw assertion errors.

## Decision outcome

**Chosen: option 1.**

- **Pipeline per case:** run (replay/rerun/live) → `DeterministicScorer`s → `ToolCallScorer` → `BudgetScorer` → `JudgeScorer` (only if the case declares a rubric). Each scorer returns `CheckResult`s (name, pass, detail) and metric values. Scorers never throw; errors become `scorer_error` checks.
- **Short-circuit (cost guard):** if a case fails a **Must** deterministic check, the judge is skipped for that case (`judge: skipped`) unless `judge.alwaysRun=true`.
- **Tool-call score:** for expected calls E and actual calls A, matched under the case's order mode (`strict`, `in-order-with-gaps`, `any-order`) and argument matcher (`exact`, `subset`, `jsonpath`, `schema`): `precision = matched/|A|`, `recall = matched/|E|`, `tool_call_score = F1`. Forbidden-tool calls and call-count violations are separate Must checks. No expectations declared → metric absent (not 1.0).
- **Metrics:** default set from §7.2. Suite aggregates: mean for scores, share for `pass_rate`, sum for cost/tokens, p50/p95 for latency (nearest-rank).
- **Case pass:** a case passes when all its **Must** checks pass. `pass_rate` = passed / scored cases (aborted cases excluded and reported separately).
- **Gate severities:** `MUST` → violation fails the build; `SHOULD` → violation is a warning in the report and job summary; `INFO` → reported only. Defaults: `pass_rate`, `accuracy` (deterministic part), `tool_call_score`, `cost_usd`, `tokens_total`, `steps` = MUST; `judge_score` = SHOULD; `latency_ms_*` = MUST in rerun/live, **INFO in replay** (replayed latency is recorded latency, not a measurement).
- **Accuracy split:** `accuracy_deterministic` and `judge_score` are reported separately so severities can differ; `accuracy` (combined) is INFO.
- **SPI:** `Scorer` interface (`ScoreResult score(EvalCase, RunResult, ScoringContext)`) via `ServiceLoader` and programmatic registration in the JUnit extension.

## Consequences

- ➕ Builds fail only on signals that are deterministic and explainable; noisy judge signals warn.
- ➕ Separate metrics keep failure reasons legible ("tool_call_score dropped", not "score 0.71").
- ➖ No single headline number; the report has to summarize several metrics.
- ➖ Skipping the judge on deterministic failure hides judge deltas for already-failing cases (documented).
- ➖ F1 treats all expected calls equally; weighted tool importance is a future option.

## Links

- [SDR-0001](../sdr/SDR-0001-agent-eval-harness.md)
- [ADR index](./README.md)
