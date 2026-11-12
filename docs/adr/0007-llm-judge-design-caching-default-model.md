# ADR-0007: LLM-as-judge: anchored rubrics, temperature 0, content-hash cache committed to git, local Ollama default

| Field | Value |
|---|---|
| **Status** | Accepted (12 Nov 2026, Gautam Gupta) |
| **Date** | 12 Nov 2026 IST |
| **Deciders** | Gautam Gupta |
| **Requirements** | [SDR-0001 v1.0](../sdr/SDR-0001-agent-eval-harness.md) |

## Context

FR-022–024 require rubric scoring with recorded judge metadata, a judge cache, and a pairwise mode with position swap. RD-010: commit the judge cache for the example and recommend it for users. RD-011: local Ollama judge for the example and CI, OpenAI optional via env, exact model names pinned in an ADR. SEC-008: judges see redacted content only. §7.3 requires a calibration set.

## Decision drivers

- FR-022, FR-023, FR-024, RD-010, RD-011, SEC-008, NFR-002, NFR-004, NFR-009, AC-006, R-003.

## Considered options

1. **Rubric files + OpenAI-compatible judge client + content-addressed cache files.**
2. Use a framework (Spring AI / LangChain4j) as the judge client.
3. No cache; always call the judge.

## Decision outcome

**Chosen: option 1.**

- **Rubrics:** versioned JSON files (no YAML in v1, consistent with RD-005) `aeh/rubrics/<id>.json` with `id`, `version`, `criteria[]`, and anchored levels **1–5** (each level has a written description). Score normalized to 0–1 as `(s−1)/4`.
- **Judge call:** OpenAI-compatible Chat Completions via `aeh-judge`'s own small JDK `HttpClient` client (no framework dependency), `temperature=0`, JSON response format requested; required output `{ "score": 1-5, "rationale": "<= 300 chars" }`. Invalid output → one retry with a repair prompt → else `judge_error` (SHOULD-severity warning; never a silent pass).
- **Pairwise:** candidate vs baseline answer judged twice with positions swapped; result = win / loss / tie only when both orders agree, else `tie` and flagged `inconsistent`.
- **Default models (pinned here, RD-011):** judge = Ollama `qwen2.5:7b-instruct`; example agent = Ollama `llama3.1:8b` (different families so the model doesn't grade itself). OpenAI judge is opt-in via `AEH_JUDGE_BASE_URL`, `AEH_JUDGE_MODEL`, `OPENAI_API_KEY`; no OpenAI model name is hard-coded as a default. Pinned names are revisited at each minor release.
- **Cache (RD-010):** key = SHA-256 over (rubric id + version, judge model id, judge prompt template version, redacted input hash, redacted output hash, mode). One JSON file per entry under `src/test/resources/aeh/judge-cache/<first-2-hex>/<hash>.json` holding score, rationale, model, timestamp. Committed for the example; documented as recommended. In `replay` a cache miss is a `judge_cache_miss` warning (no live call) unless `aeh.judge.allowLiveInReplay=true`.
- **Privacy:** judge input is built only from redacted cassette/run content (SEC-008).
- **Calibration set:** `aeh/calibration/` cases with human-assigned scores; a CI job reports mean absolute error between judge and human labels as a warning when above a configurable threshold. The SDR commits to no agreement number; the measured value is reported in README.

## Consequences

- ➕ PR CI replays judge scores at zero cost and deterministically.
- ➕ No framework lock-in for the judge path.
- ➖ A 7B local judge is weaker than hosted frontier models; that's why judge gates default to SHOULD (ADR-0006).
- ➖ Committed cache files add repo churn when outputs change.
- ➖ Pinned Ollama tags can change upstream; README documents how to pin a digest.

## Links

- [SDR-0001](../sdr/SDR-0001-agent-eval-harness.md)
- [ADR index](./README.md)
