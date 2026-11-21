plugins {
    id("io.github.guptagautam768.java-conventions")
    id("io.github.guptagautam768.publishing-conventions")
}

dependencies {
    api(libs.jackson.databind)
    api(libs.slf4j.api)
    testImplementation(libs.json.schema.validator)
}
