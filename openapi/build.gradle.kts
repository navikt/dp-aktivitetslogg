plugins {
    id("common")
    id("org.openapi.generator") version "7.13.0"
    `java-library`
}

repositories {
    mavenCentral()
}

tasks {
    compileKotlin {
        dependsOn("openApiGenerate")
    }
}

tasks.named("runKtlintCheckOverMainSourceSet").configure {
    dependsOn("openApiGenerate")
}

tasks.named("runKtlintFormatOverMainSourceSet").configure {
    dependsOn("openApiGenerate")
}

ktlint {
    filter {
        exclude { element -> element.file.path.contains("generated") }
    }
}

sourceSets {
    main {
        java {
            setSrcDirs(listOf("${layout.buildDirectory.get()}/generated/src/main/kotlin"))
        }
    }
}

dependencies {
    implementation(libs.jackson.annotation)
}

openApiGenerate {
    generatorName.set("kotlin")
    inputSpec.set("$projectDir/src/main/resources/aktivitetslogg-api.yaml")
    outputDir.set("${layout.buildDirectory.get()}/generated/")
    packageName.set("no.nav.dagpenger.aktivitetslogg.api")
    globalProperties.set(
        mapOf(
            "apis" to "none",
            "models" to "",
        ),
    )
    typeMappings =
        mapOf(
            "DateTime" to "LocalDateTime",
        )
    importMappings =
        mapOf(
            "LocalDateTime" to "java.time.LocalDateTime",
        )
    modelNameSuffix.set("DTO")
    configOptions.set(
        mapOf(
            "dateLibrary" to "custom",
            "serializationLibrary" to "jackson",
            "enumPropertyNaming" to "original",
        ),
    )
}
