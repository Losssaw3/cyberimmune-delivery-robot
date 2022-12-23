plugins {
    id("delivery.app-conventions")
}

version = "1.0"
group = "ru.bardinpetr.delivery.backend"

dependencies {
    implementation(project(":common:libs:crypto"))
    implementation(project(":common:libs:messages"))
}