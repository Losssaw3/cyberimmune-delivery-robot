plugins {
    id("java")
    application
}

group = "ru.bardinpetr.delivery.backend.authentication"
version = "v1.0"

repositories {
    mavenCentral()
}

application {
    mainClass.set("${group}.Main")
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}