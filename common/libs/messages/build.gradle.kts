plugins {
    id("delivery.app-conventions")
}

group = "ru.bardinpetr.delivery.common.libs.messages"
version = "v0.1"

dependencies {
    implementation("org.springframework.kafka:spring-kafka:2.8.10")
    implementation("org.apache.kafka:kafka-clients:3.3.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.14.0")
}
