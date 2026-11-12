# ADR-0003: Cassette format and storage: pretty-printed JSON, one file per case, committed to the user's repo

| Field | Value |
|---|---|
| **Status** | Accepted (12 Nov 2026, Gautam Gupta) |
| **Date** | 12 Nov 2026 IST |
| **Deciders** | Gautam Gupta |
| **Requirements** | [SDR-0001 v1.0](../sdr/SDR-0001-agent-eval-harness.md) |

## Context

FR-001–003 and DM-001–004 define what a recording holds; RD-005 fixes pretty-printed JSON, one file per case, stable key order, no YAML in v1; NFR-007 requires `schemaVersion` with N-1 read support; DATA-001 says cassettes are committed. Diffs must be reviewable in a PR.

## Decision drivers

- RD-005, FR-003, FR-005, DM-001…DM-004, NFR-002, NFR-007, DATA-001, DATA-006, R-009.

## Considered options

1. **One JSON file per case** containing all runs (samples) of that case.
2. JSONL, one line per step.
3. One file per run, or a binary/compressed archive.

## Decision outcome

**Chosen: option 1.**

- **Layout (defaults, configurable, CFG-002):** `src/test/resources/aeh/cassettes/<suite>/<caseId>.json`. `caseId` is restricted to `[a-z0-9._-]` so file names are portable.
- **Shape:** `Cassette { schemaVersion, caseId, suite, recordedAt, harnessVersion, agent, integration, redaction, runs[] }`; each `Run` holds `steps[]` as a single ordered list of tagged `LlmCall` / `ToolCall` objects (`"type"` discriminator) so the timeline order is explicit.
- **Determinism:** Jackson with sorted map keys, fixed property order on model classes, 2-space indent, `\n` line endings, trailing newline, UTF-8. Money is `BigDecimal` serialized as a plain decimal string; timestamps are UTC ISO-8601 with millisecond precision. Re-writing an unchanged cassette yields a byte-identical file.
- **Large payloads:** inline in v1 (no side files). A warning is logged above a configurable size (default 1 MiB per cassette).
- **Request hash:** `requestHash` = SHA-256 of the **normalized, redacted** request (ADR-0004 defines normalization).
- **Schema versioning:** `schemaVersion` is `major.minor`. Readers accept the current and previous minor; a major bump ships `aeh migrate` (CLI). A JSON Schema per version is published in `schemas/`.
- **Writes are atomic:** write to a temp file *in memory-redacted form only*, then move (DATA-006: no unredacted bytes on disk).

## Consequences

- ➕ Human-diffable; one case changes one file; easy to delete or re-record a single case.
- ➕ Works with any VCS and CI cache; no extra tooling.
- ➖ Large tool results (e.g. long documents) bloat the repo; acceptable for v1, side-file blobs are a future option.
- ➖ JSON is noisier to read by hand than YAML; the HTML report (ADR-0011) is the friendly view.
- ➖ Recording the same case again rewrites the whole file, so the PR diff can be large after a re-record.

## Links

- [SDR-0001](../sdr/SDR-0001-agent-eval-harness.md)
- [ADR index](./README.md)
