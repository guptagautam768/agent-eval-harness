plugins {
    id("io.github.guptagautam768.java-conventions")
    id("io.github.guptagautam768.publishing-conventions")
    application
}

application {
    mainClass.set("io.github.guptagautam768.aeh.cli.AehMain")
}

dependencies {
    implementation(project(":aeh-engine"))
    implementation(project(":aeh-judge"))
    implementation(project(":aeh-report"))
    implementation(project(":aeh-redact"))
    implementation(libs.picocli)
}
