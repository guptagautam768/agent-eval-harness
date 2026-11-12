# ADR-0011: Reports: versioned JSON schema, single-file offline HTML, JUnit XML and Markdown summary

| Field | Value |
|---|---|
| **Status** | Accepted (12 Nov 2026, Gautam Gupta) |
| **Date** | 12 Nov 2026 IST |
| **Deciders** | Gautam Gupta |
| **Requirements** | [SDR-0001 v1.0](../sdr/SDR-0001-agent-eval-harness.md) |

## Context

FR-060–062 require a schema-versioned JSON report and a self-contained HTML report with no CDN calls, built only from redacted data. FR-045 asks for JUnit XML; CI-001 for a job summary.

## Decision drivers

- G-008, FR-060, FR-061, FR-062, FR-045, CI-001, NFR-007, AC-011.

## Considered options

1. **JSON as the source of truth; HTML, JUnit XML and Markdown rendered from it.** HTML is a template with inline CSS/JS and inline SVG charts, no framework.
2. HTML built with a JS framework bundle (React/Vue) inlined.
3. Server-rendered or hosted report.

## Decision outcome

**Chosen: option 1**, module `aeh-report`.

- **Output dir:** `build/reports/aeh/<suite>/` (Maven: `target/reports/aeh/<suite>/`): `report.json`, `report.html`, `junit.xml`, `summary.md`.
- **JSON (DM-008):** `schemaVersion`, `summary` (counts, aggregates, gate result), `results[]`, `gate.violations[]`, `warnings[]`, `environment` (JDK, OS, harness version, git sha if available), `configFingerprint`. JSON Schema (draft 2020-12) published in `schemas/report-<version>.schema.json` and validated in tests.
- **HTML:** single file, inline CSS and a small vanilla-JS script, the JSON embedded in a `<script type="application/json">` block; sections: summary + gate, per-case table with baseline deltas, per-case drill-down (timeline of LLM/tool steps, check results, judge rationale), cost/latency charts as inline SVG. All embedded content HTML-escaped. Strict `Content-Security-Policy` meta allowing no network.
- **JUnit XML:** one `testcase` per eval case; Must failures as `failure`, Should violations as `system-out` notes.
- **Markdown summary:** short gate table for `$GITHUB_STEP_SUMMARY` and optional PR comment.

## Consequences

- ➕ Report opens offline from a CI artifact; no supply-chain exposure from CDNs.
- ➕ JSON-first makes it easy for others to build dashboards later (F-005).
- ➖ Hand-written vanilla JS/SVG is more effort than a chart library; kept small on purpose.
- ➖ Very large suites produce large single-file HTML; acceptable for v1.

## Links

- [SDR-0001](../sdr/SDR-0001-agent-eval-harness.md)
- [ADR index](./README.md)
