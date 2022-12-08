plugins {
    id("delivery.app-conventions")
}

version = "1.0"
group = "ru.bardinpetr.delivery.backend.authentication"

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")

    implementation(project(":libs:crypto"))
    implementation(project(":libs:messages"))
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}