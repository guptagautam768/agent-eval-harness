# Contributing

`agent-eval-harness` is spec-driven. [SDR-0001](docs/sdr/SDR-0001-agent-eval-harness.md) says what the system does. The [ADRs](docs/adr/README.md) say how. Do not implement a behavior that contradicts an accepted ADR. If a change needs a judgment the ADRs do not cover, add a new ADR in `Proposed` status in the same pull request and wait for it to be accepted before merge.

## Build

Use JDK 21. Library code targets Java 17 bytecode, so it cannot use newer language features or APIs. Example projects target Java 21.

```bash
./gradlew build
./gradlew spotlessApply
```

`./gradlew build` is the check that must pass: unit tests, Spotless, JaCoCo, and the license report. The license check fails closed on GPL and AGPL by allowing only the permissive licenses listed in `config/allowed-licenses.json`.

Pull request CI runs that build on Java 21 and on a Java 17 test runtime, plus CodeQL and dependency review.

## Commits

Use [Conventional Commits](https://www.conventionalcommits.org/): `feat:`, `fix:`, `test:`, `docs:`, `ci:`, `chore:`. Keep each commit focused. The history is part of the project.

## Tests

Add unit tests next to the code they cover. Redaction tests must use synthetic secrets and synthetic PII only. Do not commit real keys, tokens, or personal data. Cassettes are redacted before write; `aeh lint` must still pass on anything committed under the repo.

## Modules

`aeh-core` depends only on Jackson and the SLF4J API. Framework dependencies stay in their own modules. Do not add Spring, LangChain4j, or a provider SDK to the core.
