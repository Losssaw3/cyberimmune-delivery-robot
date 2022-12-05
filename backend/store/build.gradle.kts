plugins {
    application
    id("java")
}

group = "ru.bardinpetr.delivery.backend.store"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

application {
    mainClass.set("ru.bardinpetr.delivery.backend.store.Main")
}

tasks.register("prepareKotlinBuildScriptModel") {}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.8.9")
    implementation(project(":libs:crypto"))
}
