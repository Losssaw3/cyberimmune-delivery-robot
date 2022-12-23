plugins {
    id("delivery.app-conventions")
}

version = "1.0"
group = "ru.bardinpetr.delivery.backend"

dependencies {
    implementation(project(":libs:crypto"))
    implementation(project(":libs:messages"))
}