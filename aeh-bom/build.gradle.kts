plugins {
    `java-platform`
    `maven-publish`
}

dependencies {
    constraints {
        api(project(":aeh-core"))
        api(project(":aeh-redact"))
        api(project(":aeh-engine"))
        api(project(":aeh-judge"))
        api(project(":aeh-report"))
        api(project(":aeh-junit5"))
        api(project(":aeh-http"))
        api(project(":aeh-spring-ai"))
        api(project(":aeh-langchain4j"))
        api(project(":aeh-cli"))
        api(project(":aeh-maven-plugin"))
        api(project(":aeh-gradle-plugin"))
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["javaPlatform"])
            pom {
                name.set("aeh-bom")
                description.set("Bill of materials for agent-eval-harness")
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
