plugins {
    id("delivery.app-conventions")
}

group = "ru.bardinpetr.delivery.e2e"
version = "1.0"

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
    implementation(project(":common:libs:messages"))
    implementation("org.springframework.kafka:spring-kafka:2.8.10")
    implementation("org.apache.kafka:kafka-clients:3.3.1")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    enabled = gradle.startParameter.taskNames.any { it.contains("test") }
}