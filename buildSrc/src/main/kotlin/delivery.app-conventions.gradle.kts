plugins {
    id("delivery.java-conventions")
    application

    id("com.palantir.docker")
}

dependencies {
    implementation("org.apache.logging.log4j:log4j-api:2.6.1")
    implementation("org.apache.logging.log4j:log4j-core:2.6.1")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.6.1")
}

application {
    val pkg = project.path.replace(":", ".").substring(1)
    mainClass.set("ru.bardinpetr.delivery.${pkg}.Main")
}

tasks.distZip {
    archiveFileName.set("package.zip")
}

docker {
    name = "delivery-${project.name}"
    buildArgs(
        mapOf(
            "PROJECT_NAME" to project.name
        )
    )
    setDockerfile(file(project.rootProject.file("docker/java.Dockerfile")))
    files(tasks.distZip)
}

afterEvaluate {
    copy {
        from(project.rootProject.file("resources/log4j2.xml"))
        into(layout.projectDirectory.file("src/main/resources"))
    }
}
