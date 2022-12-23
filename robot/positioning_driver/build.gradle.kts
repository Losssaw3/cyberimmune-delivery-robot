plugins {
    id("delivery.lib-conventions")
}

group = "ru.bardinpetr.delivery.robot"
version = "1.0"

dependencies {
    implementation(project(":common:libs:messages"))
}