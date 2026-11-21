plugins {
    base
    alias(libs.plugins.license.report)
}

group = "io.github.guptagautam768"
version = "0.1.0-SNAPSHOT"

val junitJupiter = libs.junit.jupiter
val junitPlatformLauncher = libs.junit.platform.launcher
val assertjCore = libs.assertj.core
val slf4jSimple = libs.slf4j.simple

subprojects {
    group = rootProject.group
    version = rootProject.version
    plugins.withId("java") {
        dependencies {
            add("testImplementation", junitJupiter)
            add("testImplementation", assertjCore)
            add("testRuntimeOnly", slf4jSimple)
            // Gradle 8's bundled launcher is older than JUnit 5.14.
            add("testRuntimeOnly", junitPlatformLauncher)
        }
    }
}

licenseReport {
    configurations = arrayOf("runtimeClasspath")
    excludeOwnGroup = true
    excludeBoms = true
    allowedLicensesFile = layout.projectDirectory.file("config/allowed-licenses.json")
}

tasks.named("checkLicense") {
    dependsOn("generateLicenseReport")
}

tasks.named("build") {
    dependsOn("checkLicense")
}
