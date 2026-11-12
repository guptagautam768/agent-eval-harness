# ADR-0002: Build and packaging: Java 21 toolchain targeting Java 17, Gradle multi-module, Maven Central + Gradle Plugin Portal

| Field | Value |
|---|---|
| **Status** | Accepted (12 Nov 2026, Gautam Gupta) |
| **Date** | 12 Nov 2026 IST |
| **Deciders** | Gautam Gupta |
| **Requirements** | [SDR-0001 v1.0](../sdr/SDR-0001-agent-eval-harness.md) |

## Context

The harness is a **library and build tool**, not a service: users pull it into their own test classpath and build. RD-004 fixes a Java 21 toolchain emitting Java 17 bytecode; RD-003 fixes publishing to Maven Central under `io.github.guptagautam768` plus the Gradle Plugin Portal; NFR-006 wants a small core with optional integration modules; §9.2 proposes a module list.

## Decision drivers

- RD-003, RD-004, NFR-005, NFR-006, NFR-011, G-006, §9.2, AC-010, AC-014.

## Considered options

1. **Gradle (Kotlin DSL) multi-module, version catalog, `options.release = 17` on a Java 21 toolchain**, publish via Central Portal.
2. Maven multi-module reactor.
3. Single fat artifact with optional dependencies.

## Decision outcome

**Chosen: option 1.**

- **Toolchain:** Gradle Java toolchain 21 for compiling and running the build; every published module compiles with `--release 17`. Example projects compile with release 21.
- **Modules** (artifact ids, all under group `io.github.guptagautam768`, Java packages under `io.github.guptagautam768.aeh`): `aeh-core`, `aeh-redact`, `aeh-engine`, `aeh-judge`, `aeh-report`, `aeh-junit5`, `aeh-http`, `aeh-spring-ai`, `aeh-langchain4j`, `aeh-cli`, `aeh-maven-plugin`, plus the Gradle plugin `io.github.guptagautam768.agent-eval` (`aeh-gradle-plugin`). Not published: `examples/*`, `internal-test-fixtures`.
- **BOM:** `aeh-bom` so users align versions with one coordinate.
- **Dependency rules:** `aeh-core` depends only on Jackson and SLF4J API. Framework dependencies (Spring AI, LangChain4j) are `compileOnly`/`api` only in their own modules. Supported framework versions are the ones tested in CI (see ADR-0012), documented in README.
- **Build conventions** live in `build-logic/` (included build): Java conventions, publishing, signing, Spotless, JaCoCo.
- **Publishing:** `maven-publish` + signing to the **Sonatype Central Portal**; Gradle plugin via `com.gradle.plugin-publish`. Signing key and portal tokens are GitHub Actions secrets, used only by a release workflow triggered by a version tag. Fallback per RD-003: if Central setup blocks v1.0.0, attach jars to the GitHub Release and rely on JitPack, then publish to Central in v1.0.1.
- **Versioning:** SemVer; `0.x` snapshots during weeks 1–5 are not published.

## Consequences

- ➕ Java 17 consumers can adopt it; Spring AI 1.x and LangChain4j 1.x both support 17.
- ➕ Users pull only the integrations they use.
- ➖ Library code cannot use Java 18–21 APIs or language features (e.g. record patterns, pattern matching for switch as finalized in 21, virtual-thread APIs). CI must also run tests on a Java 17 runtime to catch accidental API use (ADR-0012).
- ➖ Maven Central needs a one-time setup by Gautam: Central Portal account, namespace verification for `io.github.guptagautam768`, a GPG signing key, and repo secrets. **The bot cannot do these steps.**
- ➖ Shipping a Maven plugin from a Gradle build needs extra wiring (plugin descriptor generation); covered in ADR-0010.

## Links

- [SDR-0001](../sdr/SDR-0001-agent-eval-harness.md)
- [ADR index](./README.md)
