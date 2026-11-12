# ADR Review Checklist — `agent-eval-harness`

**Reviewer:** Gautam Gupta  
**Set under review:** ADR-0001 … ADR-0013 (all *Accepted* as-is by Gautam Gupta, 12 Nov 2026 IST; judgment calls in §B confirmed)  
**Requirements baseline:** SDR-0001 v1.0 (APPROVED, 12 Nov 2026)

## A. Cross-cutting

- [ ] Every Must FR/NFR/SEC in the SDR is covered by at least one ADR.
- [ ] No ADR contradicts a resolved decision (RD-001…RD-014).
- [ ] Trade-offs are honest; no invented benchmark numbers.
- [ ] Module layout (ADR-0002) is the right size for a 6-week MVP.

## B. Judgment calls the SDR didn't settle (need an explicit yes/no)

- [ ] Maven group `io.github.guptagautam768`, Java package `io.github.guptagautam768.aeh`, artifact prefix `aeh-`, Gradle plugin id `io.github.guptagautam768.agent-eval`, plus an `aeh-bom` — ADR-0002.
- [ ] Default file layout under `src/test/resources/aeh/` (cassettes, baselines, judge-cache, rubrics, calibration) — ADR-0003/0007/0008.
- [ ] `record` mode refused when `CI=true` unless explicitly allowed — ADR-0004.
- [ ] Ordered matching by default; `unordered` opt-in for parallel agents — ADR-0004.
- [ ] HTTP capture via an embedded localhost proxy on the JDK `HttpServer` (needs a base-URL change in the agent) — ADR-0005.
- [ ] Tool-call score = F1 of precision/recall; judge skipped when a Must deterministic check already failed — ADR-0006.
- [ ] Latency gates are INFO in replay (MUST only in rerun/live) — ADR-0006/0008.
- [ ] `steps` metric gated at +10% MUST (RD-013 didn't list steps) — ADR-0008.
- [ ] Gates on suite aggregates, not per case (pass→fail cases listed prominently) — ADR-0008.
- [ ] Promote refuses on MUST violations unless `--allow-regression` — ADR-0008.
- [ ] Pinned Ollama models: judge `qwen2.5:7b-instruct`, demo agent `llama3.1:8b`; no default OpenAI model name — ADR-0007/0013.
- [ ] Judge scale 1–5 anchored, normalized to 0–1; judge −0.05 tolerance applies on the 0–1 scale — ADR-0007/0008.
- [ ] High-entropy secret heuristic limited to headers and key-like field names (≥32 chars, ≥4.0 bits/char) — ADR-0009.
- [ ] New exit code **4** = run aborted (cost ceiling / unrecoverable provider error), beyond the SDR's 0–3 — ADR-0010.
- [ ] Separate `agentEval` Gradle source set, `check` runs it by default — ADR-0010.
- [ ] CLI ships as a runnable jar, no native image in v1 — ADR-0010.
- [ ] HTML report hand-written (vanilla JS + inline SVG, no chart library) — ADR-0011.
- [ ] Live Ollama tests nightly/manual only, not on PRs; Java 17 runtime job in PR CI — ADR-0012.
- [ ] Demo adds a 4th tool `escalateToHuman` (for forbidden-tool checks) and a starter suite of about a dozen cases — ADR-0013.

## C. Per-ADR decisions

| ADR | Approve | Changes requested |
|---|---|---|
| 0001 Record architecture decisions | [ ] | |
| 0002 Build and packaging | [ ] | |
| 0003 Cassette format and storage | [ ] | |
| 0004 Run modes and request matching | [ ] | |
| 0005 Integration SPI and adapter priority | [ ] | |
| 0006 Scoring pipeline and gate severities | [ ] | |
| 0007 LLM-as-judge | [ ] | |
| 0008 Baselines, tolerances, promote | [ ] | |
| 0009 Redaction | [ ] | |
| 0010 Entry points and exit codes | [ ] | |
| 0011 Reports | [ ] | |
| 0012 Testing, CI and licensing | [ ] | |
| 0013 Demo agent | [ ] | |

## D. Gautam-only actions before go-live (bot cannot do these)

- Sonatype Central Portal account + verify namespace `io.github.guptagautam768`.
- GPG signing key and Gradle Plugin Portal API key, stored as repo secrets.

## After approval

1. Mark ADRs Accepted.
2. Create the public `agent-eval-harness` repo (on Gautam's word).
3. Week 1 PR per SDR §11.
