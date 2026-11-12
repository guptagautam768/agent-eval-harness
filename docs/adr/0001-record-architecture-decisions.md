# ADR-0001: Record architecture decisions

| Field | Value |
|---|---|
| **Status** | Accepted (12 Nov 2026, Gautam Gupta) |
| **Date** | 12 Nov 2026 IST |
| **Deciders** | Gautam Gupta |
| **Requirements** | [SDR-0001 v1.0](../sdr/SDR-0001-agent-eval-harness.md) |

## Context

SDR-0001 (APPROVED v1.0, 12 Nov 2026) fixes *what* agent-eval-harness must do. The *how* (formats, modes, scoring, packaging, entry points) needs a durable, reviewable record for future contributors and for portfolio readers who want the reasoning, not only the code (G-011, AC-015). The same process worked for `jvm-llm-gateway`.

## Decision drivers

- G-011: recruiter/interview-ready; trade-offs visible.
- AC-015: README links the SDR and the ADRs.
- Spec-driven flow: SDR → ADR → weekly PRs; nothing is implemented without an Accepted ADR.

## Considered options

1. **MADR-style ADRs in `docs/adr/`**, numbered, Markdown, reviewed via a `REVIEW.md` checklist.
2. Single living design doc.
3. Decisions only in PR descriptions / GitHub issues.

## Decision outcome

**Chosen: option 1.** Each ADR uses the same template: Status, Date, Deciders, Context, Decision drivers (citing SDR IDs), Considered options, Decision outcome, Consequences, Links. Lifecycle: `Proposed → Accepted` (or `Rejected`); later changes create a new ADR that marks the old one `Superseded by ADR-NNNN` rather than editing history. Judgment calls made during a weekly PR that no ADR covers are written up as new **Proposed** ADRs in that PR and accepted before merge.

## Consequences

- ➕ Reasoning is versioned with the code and diff-reviewable.
- ➕ Each requirement is traceable to a decision (SDR IDs in drivers).
- ➖ Overhead of writing ~13 docs; risk of drift if code changes without a superseding ADR.
- ➖ Less readable end-to-end than one design doc; mitigated by the index in [README.md](./README.md).

## Links

- [SDR-0001](../sdr/SDR-0001-agent-eval-harness.md)
- [ADR index](./README.md)
