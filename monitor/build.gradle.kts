plugins {
    application
}

//group = "${group}.monitor"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.kafka:kafka-clients:3.3.1")
    implementation("org.apache.kafka:kafka-streams:3.3.1")
    implementation("org.slf4j:slf4j-nop:2.0.3")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.14.0")
}

application {
    mainClass.set("ru.bardinpetr.cyberimmune_delivery.monitor.Main")
}