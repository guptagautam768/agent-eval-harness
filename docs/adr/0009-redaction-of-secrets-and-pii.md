# ADR-0009: Redaction: in-memory, before write; typed stable placeholders; redact-and-continue default; lint gate

| Field | Value |
|---|---|
| **Status** | Accepted (12 Nov 2026, Gautam Gupta) |
| **Date** | 12 Nov 2026 IST |
| **Deciders** | Gautam Gupta |
| **Requirements** | [SDR-0001 v1.0](../sdr/SDR-0001-agent-eval-harness.md) |

## Context

SEC-001–010 and FR-006 require redaction before any byte hits disk, secret and PII coverage aligned with jvm-llm-gateway ADR-0009, consistent placeholders without a reversible mapping, an allowlist mode, a lint scanner, and custom rules. RD-009: redact and continue with a warning and counts; `aeh lint` still fails CI; `fail` mode is opt-in.

## Decision drivers

- FR-006, FR-062, SEC-001…SEC-010, RD-009, NFR-003, NFR-004, DATA-006, AC-009, R-004.

## Considered options

1. **Rule-based redactor (headers + regex/validators + entropy heuristic) applied to the in-memory model, with per-cassette placeholder numbering.**
2. External detector service (e.g. Presidio) in the record path.
3. Hash-based placeholders (e.g. HMAC of the value).

## Decision outcome

**Chosen: option 1**, in module `aeh-redact`.

- **Where:** the recorder builds the cassette in memory, runs the redactor over every string field (messages, tool args/results, params, headers, errors), then serializes. Reports and logs are built only from redacted objects.
- **Headers:** drop `Authorization`, `Proxy-Authorization`, `x-api-key`, `api-key`, `Cookie`, `Set-Cookie`, plus configured names. Allowlist mode (SEC-005): only listed headers/fields are kept.
- **Secret rules:** OpenAI-style (`sk-…`) and Anthropic-style (`sk-ant-…`) keys, AWS access key ids + secret pattern near them, GitHub tokens (`ghp_`, `gho_`, `ghs_`, `github_pat_`), JWTs, PEM private-key blocks, URLs with embedded credentials. **Generic high-entropy heuristic:** tokens ≥ 32 chars with Shannon entropy ≥ 4.0 bits/char and no whitespace, **only in header values and in JSON fields whose name matches `(?i)(key|token|secret|password|auth)`**, to limit false positives on IDs and hashes.
- **PII rules (same categories as jvm-llm-gateway ADR-0009):** email, phone (E.164 + Indian formats), card numbers (Luhn-validated), IPv4/IPv6, IBAN (checksum-validated), PAN, Aadhaar (Verhoeff-validated).
- **Placeholders:** `<TYPE_n>` (e.g. `<EMAIL_1>`, `<SECRET_2>`). Within one cassette, the same original value gets the same `n`, using an in-memory map discarded after write (SEC-004). Nothing reversible is persisted. In replay/rerun the live request is redacted with a fresh map in the same traversal order, so placeholders line up for hashing (ADR-0004).
- **Detection policy (RD-009):** default `redact`: replace, continue, log `WARN` with counts per type (never the values), and record counts in `cassette.redaction`. Opt-in `fail`: abort recording that case.
- **Lint (SEC-006):** `aeh lint` / `agentEvalLint` scans cassettes, baselines, judge cache and reports with the same rules (placeholders ignored) and exits **3** on any hit. Gradle `check` depends on it by default.
- **Extensibility (SEC-009):** custom regex rules in config; `Redactor` SPI for code-based detectors.

## Consequences

- ➕ Safe-by-default commits; the lint gate catches what the recorder missed or what someone hand-edited in.
- ➕ No reversible mapping means a leaked cassette doesn't leak originals.
- ➖ Regex redaction is best-effort: names, addresses and free-text PII are not detected (documented, SEC-010).
- ➖ Redacting test inputs changes what the model saw vs what is stored; replay still works because matching uses redacted forms on both sides, but a redacted placeholder in a recorded *response* may read oddly.
- ➖ Order-dependent placeholder numbering can mismatch if the agent reorders content; the mismatch diff makes it visible.

## Links

- [SDR-0001](../sdr/SDR-0001-agent-eval-harness.md)
- [ADR index](./README.md)
