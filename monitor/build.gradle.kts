plugins {
    id("java")
    application
}

group = "ru.bardinpetr.delivery.monitor"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.kafka:kafka-clients:3.3.1")
    implementation("org.apache.kafka:kafka-streams:3.3.1")
    implementation("org.springframework.kafka:spring-kafka:2.8.10")

    implementation("org.slf4j:slf4j-nop:2.0.3")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.14.0")

    implementation(project(":libs:messages"))
}

application {
    mainClass.set("${group}.Main")
}