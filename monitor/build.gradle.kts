plugins {
    id("delivery.app-conventions")
}

group = "ru.bardinpetr.delivery.monitor"
version = "0.1"

dependencies {
    implementation("org.apache.kafka:kafka-clients:3.3.1")
    implementation("org.springframework.kafka:spring-kafka:2.8.10")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.14.0")

    implementation(project(":libs:messages"))
}
