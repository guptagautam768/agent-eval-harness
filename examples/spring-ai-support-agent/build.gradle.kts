plugins {
    id("io.github.guptagautam768.java-conventions")
}

// Example projects compile for Java 21 (ADR-0002). Library modules stay on release 17.
tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}

dependencies {
    implementation(project(":aeh-core"))
}
