plugins {
    id("delivery.app-conventions")
}

group = "ru.bardinpetr.delivery.robot"
version = "1.0"

dependencies {
    implementation(project(":libs:messages"))
    implementation("io.javalin:javalin:5.2.0")
}