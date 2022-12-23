plugins {
    id("delivery.app-conventions")
}

version = "1.0"
group = "ru.bardinpetr.delivery.backend"


dependencies {
    implementation(project(":libs:crypto"))
    implementation(project(":libs:messages"))

    implementation("com.fasterxml.jackson.core:jackson-databind:2.12.7.1")
    implementation("io.javalin:javalin:5.2.0")
}
