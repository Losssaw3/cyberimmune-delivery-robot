plugins {
    id("delivery.lib-conventions")
}

version = "1.0"
group = "ru.bardinpetr.delivery.backend"


dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.8.9")
    implementation(project(":libs:crypto"))
    implementation(project(":libs:messages"))
}
