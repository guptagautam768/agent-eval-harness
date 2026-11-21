pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    includeBuild("build-logic")
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
}

rootProject.name = "agent-eval-harness"

include(
    "aeh-bom",
    "aeh-core",
    "aeh-redact",
    "aeh-engine",
    "aeh-judge",
    "aeh-report",
    "aeh-junit5",
    "aeh-http",
    "aeh-spring-ai",
    "aeh-langchain4j",
    "aeh-cli",
    "aeh-maven-plugin",
    "aeh-gradle-plugin",
    "internal-test-fixtures",
    "examples:spring-ai-support-agent",
)
