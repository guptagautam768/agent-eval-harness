# ADR-0014: Redaction traversal order and overlap resolution

| Field | Value |
|---|---|
| **Status** | Proposed |
| **Date** | 12 Nov 2026 IST |
| **Deciders** | Gautam Gupta |
| **Requirements** | [SDR-0001 v1.0](../sdr/SDR-0001-agent-eval-harness.md) |

## Context

[ADR-0009](./0009-redaction-of-secrets-and-pii.md) says that, within one cassette, the same original value maps to the same `<TYPE_n>` placeholder, and that replay redacts the live request with a fresh map **in the same traversal order** so the numbers line up for hashing. It does not define that order, and it does not say which detection wins when two patterns overlap (a Luhn-valid card is also a digit run that can look like a phone).

Week 1 implements the redactor. The numbering has to be deterministic now, because week 2 request hashes depend on it (ADR-0003, ADR-0004).

## Decision drivers

- SEC-004, ADR-0009, ADR-0004, NFR-002.

## Considered options

1. **Depth-first walk, object keys sorted, longest match wins.**
2. Document order of keys as they arrived from the provider, and first-match wins.
3. Leave the order unspecified and accept that placeholder numbers can change between writes.

## Decision outcome

**Chosen: option 1.** Proposed for acceptance before merge.

- **Traversal.** Objects are visited depth-first. Object keys are visited in Unicode code-point order. Arrays are visited by index. Inside one string, non-overlapping spans are replaced from left to right.
- **Overlap.** The longest span wins. Equal length is broken by detector priority: PEM, Anthropic key, OpenAI key, AWS access key id, AWS secret, GitHub token, JWT, URL credentials, high-entropy token, then PII in the order credit card, Aadhaar, IBAN, PAN, IPv6, IPv4, phone, email. Custom and `ServiceLoader` detectors use a later priority unless they say otherwise.
- **Lint.** `aeh lint` parses JSON when it can, so the entropy heuristic still sees field names. A file that is not JSON is scanned with the pattern rules only; entropy is not applied to raw text, because ADR-0009 limits that heuristic to headers and key-like field names.
- **Numbering.** `n` is per placeholder type (`EMAIL`, `SECRET`, …), not per rule id. Occurrence counts on `cassette.redaction.counts` are per rule id and do not include the matched text.

## Consequences

- ➕ Rewriting a cassette does not renumber placeholders just because a JSON object was built with a different key insertion order.
- ➕ A 16-digit card is stored as `<CREDIT_CARD_n>`, not also as a phone.
- ➖ A provider that emits object keys in a meaningful order still gets them sorted in the redacted document. The file is for diffs and replay, not a byte copy of the provider payload.
- ➖ Lint on a non-JSON note will not flag a high-entropy token sitting in prose. Cassettes, baselines, and reports are JSON, which is the path SEC-006 cares about.

## Links

- [SDR-0001](../sdr/SDR-0001-agent-eval-harness.md)
- [ADR-0009](./0009-redaction-of-secrets-and-pii.md)
- [ADR index](./README.md)
