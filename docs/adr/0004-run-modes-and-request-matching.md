# ADR-0004: Run modes (record / replay / rerun / live) and request matching

| Field | Value |
|---|---|
| **Status** | Accepted (12 Nov 2026, Gautam Gupta) |
| **Date** | 12 Nov 2026 IST |
| **Deciders** | Gautam Gupta |
| **Requirements** | [SDR-0001 v1.0](../sdr/SDR-0001-agent-eval-harness.md) |

## Context

FR-007 requires explicit modes with `replay` as the CI default and no silent fallback to the network. FR-010–015 define replay (no network, recorded LLM + tool responses), rerun (live candidate model, tool results from the cassette, unmatched-tool policy, N samples) and per-suite/per-case overrides. RD-012 sets 3 samples for rerun and 1 for replay. NFR-002 requires deterministic replay; NFR-009 a live-run $ ceiling.

## Decision drivers

- FR-007, FR-010…FR-015, RD-012, NFR-002, NFR-009, CI-002, CI-004, AC-002…AC-004, R-002.

## Considered options

1. **Four explicit modes, ordered matching on (sequence, normalized request hash)**, strict by default.
2. Hash-only matching (any order).
3. Fuzzy/semantic matching of requests.

## Decision outcome

**Chosen: option 1.**

| Mode | LLM calls | Tool calls | Writes cassette | Typical use |
|---|---|---|---|---|
| `record` | live | live (real tools) | yes (redacted) | developer, locally |
| `replay` (default) | from cassette | from cassette | no | PR CI |
| `rerun` | live candidate model/prompt | from cassette; unmatched → policy | no (results only) | nightly / manual, prompt or model change |
| `live` | live | live | no | exploratory runs, smoke tests |

- **Mode resolution precedence:** system property `aeh.mode` → env `AEH_MODE` → per-case annotation/config → per-suite config → default `replay`.
- **Safety rails:** when env `CI=true`, `record` mode is refused (exit 2) unless `aeh.allowRecordInCi=true`. A missing cassette in `replay` fails the case (FR-007). In `replay` the harness installs no real transport; any attempted outbound call through an integration fails the case.
- **Normalization before hashing (replay match key):** drop volatile fields (request ids, `user`, `stream`, `stream_options`, timestamps, `seed` when not set by the user), sort JSON object keys, collapse insignificant whitespace in tool schemas, apply redaction (ADR-0009) so placeholders match. Users can add ignore paths (JSONPath) per suite.
- **Matching:** the *n*-th LLM call in a run must match the *n*-th recorded `LlmCall` by `requestHash`. Mismatch → fail with a structured diff (JSON Patch rendered as unified diff) of recorded vs actual normalized request (FR-011). Opt-in `matching: unordered` matches by hash regardless of order, for agents with parallel calls.
- **Tool replay key:** `(toolName, canonical JSON of arguments)`; first unused match wins so repeated identical calls replay in order.
- **Rerun unmatched-tool policy (FR-013):** `fail` (default) | `live` | `stub` (fixture file per tool). The chosen policy is recorded in the result.
- **Samples:** rerun default 3, replay 1 (RD-012); `live` default 1. Samples run sequentially by default; `aeh.parallelism` opt-in.
- **Cost ceiling (NFR-009):** rerun and live sum estimated cost as calls complete; when the suite exceeds `aeh.maxCostUsd` the run aborts, remaining cases are marked `aborted`, and the exit code is 4 (ADR-0010). Calls with unknown price count as zero and raise a warning.
- **Streaming:** recorded streams are stored as the assembled message plus the ordered chunk list; replay re-emits the chunks.

## Consequences

- ➕ PR CI is free and deterministic; misconfiguration fails loudly instead of spending money.
- ➕ Readable mismatch diffs make prompt drift obvious.
- ➖ Ordered matching fails for agents that issue parallel tool or LLM calls non-deterministically; they must opt into `unordered`.
- ➖ Any intentional prompt change breaks replay until the case is re-recorded; this is the intended signal but adds friction. `rerun` is the path for evaluating prompt changes.
- ➖ Cost ceiling is approximate (price table, in-flight calls may overshoot by one call).

## Links

- [SDR-0001](../sdr/SDR-0001-agent-eval-harness.md)
- [ADR index](./README.md)
