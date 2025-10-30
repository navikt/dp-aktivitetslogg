plugins {
    id("common")
    application
}

repositories {
    mavenCentral()
    maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
}

dependencies {
    implementation(project(":openapi"))
    implementation(libs.rapids.and.rivers)
    implementation(libs.kotlin.logging)
    implementation(libs.bundles.postgres)
    implementation(libs.bundles.ktor.server)

    implementation("io.ktor:ktor-server-swagger:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-client-content-negotiation:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-serialization-jackson:${libs.versions.ktor.get()}")
    implementation("com.github.navikt.tbd-libs:naisful-app:2025.09.19-13.31-61342e73")

    testImplementation(kotlin("test"))
    testImplementation(libs.bundles.ktor.client)
    testImplementation(libs.bundles.postgres.test)
    testImplementation(libs.bundles.kotest.assertions)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.mock.oauth2.server)
    testImplementation(libs.rapids.and.rivers.test)
    testImplementation("com.github.navikt.tbd-libs:naisful-test-app:2025.09.19-13.31-61342e73")
    testImplementation(libs.mockk)
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
