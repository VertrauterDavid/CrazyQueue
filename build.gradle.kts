plugins {
    `java-library`
    `maven-publish`
    alias(libs.plugins.shadow)
}

group = "net.vertrauterdavid"
version = "1.0"


repositories {
    mavenLocal()
    mavenCentral()

    // PaperMC
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly(libs.paper.api)
    compileOnly(libs.velocity.api)
    compileOnly(libs.lombok)

    annotationProcessor(libs.velocity.api)
    annotationProcessor(libs.lombok)

    implementation("redis.clients:jedis:5.1.0")
}

tasks {
    jar {
        enabled = false
    }

    shadowJar {
        archiveFileName = "${rootProject.name}-${project.version}.jar"
        archiveClassifier = null

        manifest {
            attributes["Implementation-Version"] = rootProject.version
        }
    }

    assemble {
        dependsOn(shadowJar)
    }

    withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
        options.release = 21
    }

    withType<Javadoc>() {
        options.encoding = Charsets.UTF_8.name()
    }

    defaultTasks("build")
}
