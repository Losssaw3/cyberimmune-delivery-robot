plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation("io.freefair.gradle:lombok-plugin:6.6")
    implementation("com.palantir.gradle.docker:gradle-docker:0.34.0")
}