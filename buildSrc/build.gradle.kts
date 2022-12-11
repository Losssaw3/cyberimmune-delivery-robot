plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation("io.freefair.gradle:lombok-plugin:6.6")
    implementation("com.palantir.gradle.docker:gradle-docker:0.34.0")
    implementation("org.apache.logging.log4j:log4j-api:2.6.1")
    implementation("org.apache.logging.log4j:log4j-core:2.6.1")
}