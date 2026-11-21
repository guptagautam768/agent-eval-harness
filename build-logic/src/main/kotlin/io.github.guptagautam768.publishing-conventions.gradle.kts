plugins {
    `maven-publish`
    signing
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            pom {
                name.set(project.name)
                description.set("Agent Eval Harness component ${project.name}")
                url.set("https://github.com/guptagautam768/agent-eval-harness")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("guptagautam768")
                        name.set("Gautam Gupta")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/guptagautam768/agent-eval-harness.git")
                    developerConnection.set("scm:git:ssh://git@github.com/guptagautam768/agent-eval-harness.git")
                    url.set("https://github.com/guptagautam768/agent-eval-harness")
                }
            }
        }
    }
}

signing {
    // 0.x snapshots are not published (ADR-0002). Signing keys are release secrets.
    val key = providers.gradleProperty("signingKey")
    setRequired(false)
    if (key.isPresent) {
        useInMemoryPgpKeys(key.get(), providers.gradleProperty("signingPassword").orElse("").get())
        sign(publishing.publications["maven"])
    }
}
