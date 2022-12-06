plugins {
    id("java")
    application
    id("java-library")
}

group = "ru.bardinpetr.delivery.libs.messages"
//version="v0.1.0"

repositories {
    mavenCentral()
}

application {
    mainClass.set("ru.bardinpetr.delivery.libs.messages.Main")
}

dependencies {
    implementation("org.springframework.kafka:spring-kafka:2.8.10")
    implementation("org.apache.kafka:kafka-clients:3.3.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.14.0")
}
