import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    id("org.jetbrains.kotlin.jvm")
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":openapi"))
    implementation(libs.rapids.and.rivers)
    implementation(libs.kotlin.logging)
    implementation(libs.bundles.postgres)
    implementation(libs.bundles.ktor.server)

    implementation("io.ktor:ktor-server-swagger:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.2")
    implementation("io.ktor:ktor-serialization-jackson:2.3.2")

    testImplementation(kotlin("test"))
    testImplementation(libs.bundles.ktor.client)
    testImplementation(libs.bundles.postgres.test)
    testImplementation(libs.bundles.kotest.assertions)
    testImplementation("io.ktor:ktor-server-test-host-jvm:2.3.2")
    testImplementation(libs.mock.oauth2.server)
}

tasks {
    test {
        useJUnitPlatform()
        testLogging {
            // events(TestLogEvent.STANDARD_OUT)
        }
    }
    jar {
        dependsOn(":openapi:jar")

        manifest {
            attributes["Main-Class"] = application.mainClass
        }

        archiveBaseName.set("app")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

application {
    mainClass.set("no.nav.dagpenger.aktivitetslogg.AppKt")
}

repositories {
    maven {
        url = uri("https://maven.pkg.github.com/navikt/rapids-and-rivers")
        credentials {
            val githubUser: String? by project
            val githubPassword: String? by project
            username = githubUser
            password = githubPassword
        }
    }
}
