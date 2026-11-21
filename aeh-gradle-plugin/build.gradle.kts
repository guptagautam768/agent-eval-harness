plugins {
    id("io.github.guptagautam768.java-conventions")
    `java-gradle-plugin`
}

gradlePlugin {
    plugins {
        create("agentEval") {
            id = "io.github.guptagautam768.agent-eval"
            implementationClass = "io.github.guptagautam768.aeh.gradle.AgentEvalPlugin"
            displayName = "Agent Eval Harness"
            description = "Record, replay and score LLM agent runs, and gate CI on regressions."
        }
    }
}

dependencies {
    implementation(project(":aeh-engine"))
    implementation(project(":aeh-report"))
}
