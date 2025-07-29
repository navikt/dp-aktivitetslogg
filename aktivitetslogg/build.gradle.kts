import java.net.URI

plugins {
    id("common")
    `maven-publish`
    `java-library`
}

java {
    withSourcesJar()
}

group = "no.nav.dagpenger"

dependencies {
    implementation(libs.bundles.jackson)
    testImplementation(kotlin("test"))
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.assertions.json)
}

tasks {
    test {
        useJUnitPlatform()
    }
}

publishing {
    publications {
        create<MavenPublication>("name") {
            from(components["java"])
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = URI("https://maven.pkg.github.com/navikt/dp-aktivitetslogg")
            credentials {
                val githubUser: String? by project
                val githubPassword: String? by project
                username = githubUser
                password = githubPassword
            }
        }
    }
}
