plugins {
    id("delivery.java-conventions")
    application

    id("com.palantir.docker")
}

dependencies {
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
