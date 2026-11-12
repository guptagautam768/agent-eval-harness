# ADR-0008: Baselines, tolerances and the promote workflow

| Field | Value |
|---|---|
| **Status** | Accepted (12 Nov 2026, Gautam Gupta) |
| **Date** | 12 Nov 2026 IST |
| **Deciders** | Gautam Gupta |
| **Requirements** | [SDR-0001 v1.0](../sdr/SDR-0001-agent-eval-harness.md) |

## Context

FR-030–034 require a per-suite baseline, declarative gate rules, explicit promotion never done by CI, and tag filtering. RD-013 sets default tolerances: pass_rate and tool_call_score −0.02 absolute; cost and tokens +10%; latency p95 +20%; judge score −0.05 (warn only). CI-003 says baseline changes go through PR review.

## Decision drivers

- FR-030…FR-034, RD-007, RD-013, DM-007, CI-003, AC-007, AC-008.

## Considered options

1. **One committed baseline JSON per suite + gate rules in build config; explicit promote command.**
2. Baselines fetched from the last green CI run on main (artifact).
3. Fixed absolute thresholds only, no baseline.

## Decision outcome

**Chosen: option 1.**

- **File:** `src/test/resources/aeh/baselines/<suite>.baseline.json` (DM-007): `schemaVersion`, `suite`, `createdAt`, `harnessVersion`, `configFingerprint` (hash of model, prompt template versions, scorer config, rubric versions, mode), per-case metrics, suite aggregates, and a snapshot of the gate rules used.
- **Gate rules:** declared in the Gradle/Maven extension or `aeh.json` for the CLI, per metric: `direction`, optional `floor`/`ceiling`, `tolerance` (`abs` or `pct`), `severity`. Defaults (RD-013): `pass_rate` abs −0.02 MUST; `tool_call_score` abs −0.02 MUST; `cost_usd` +10% MUST; `tokens_total` +10% MUST; `latency_ms_p95` +20% MUST in rerun/live, INFO in replay; `judge_score` abs −0.05 on the 0–1 scale SHOULD; `steps` +10% MUST.
- **What is gated:** **suite aggregates**. Per-case deltas are always shown in the report, and any case that flips pass→fail is listed prominently, but it fails the build only through `pass_rate` (or a per-case rule if the user adds one).
- **No baseline yet:** the gate checks only absolute floors/ceilings and reports "no baseline"; the build passes unless a floor/ceiling is violated.
- **Config drift:** if the candidate `configFingerprint` differs from the baseline's, the report flags "baseline produced with different config" (warning) so reviewers know deltas mix causes.
- **Promote:** `aeh baseline promote` / Gradle `agentEvalPromoteBaseline` / Maven `aeh:promote-baseline` writes the latest results as the new baseline. It **refuses when `CI=true`** (FR-033) and refuses if the latest run has MUST violations unless `--allow-regression` is passed (the flag is recorded in the baseline file for reviewers).
- **Tags:** cases tagged (`smoke`, `nightly`, …); baselines store all cases; gates compute aggregates over the filtered set and the matching baseline subset.

## Consequences

- ➕ Baseline changes are visible in PR diffs and need a human decision.
- ➕ Works offline and in forks; no dependency on CI artifact retention.
- ➖ Two PRs that both promote baselines will conflict; resolved by re-running promote after rebase.
- ➖ Relative tolerances on tiny values (e.g. cost of fractions of a cent) are noisy; users can switch a metric to `abs`.
- ➖ Gating on aggregates can hide one important case regressing while others improve; mitigated by the pass→fail list and optional per-case rules.

## Links

- [SDR-0001](../sdr/SDR-0001-agent-eval-harness.md)
- [ADR index](./README.md)
