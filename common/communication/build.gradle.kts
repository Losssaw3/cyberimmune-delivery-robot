plugins {
    id("delivery.app-conventions")
}

group = "ru.bardinpetr.delivery.common"
version = "1.0"

dependencies {
    implementation(project(":common:libs:messages"))
    implementation("io.javalin:javalin:5.2.0")
}