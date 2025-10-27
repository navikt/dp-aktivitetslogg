plugins {
    id("ch.acanda.gradle.fabrikt") version "1.22.0"
    id("common")
    `java-library`
}

tasks {
    compileKotlin {
        dependsOn("fabriktGenerate")
    }
}

tasks.named("runKtlintCheckOverMainSourceSet").configure {
    dependsOn("fabriktGenerate")
}

tasks.named("runKtlintFormatOverMainSourceSet").configure {
    dependsOn("fabriktGenerate")
}

sourceSets {
    main {
        java {
            setSrcDirs(listOf("src/main/kotlin", "${layout.buildDirectory.get()}/generated/src/main/kotlin"))
        }
    }
}

ktlint {
    filter {
        exclude { element -> element.file.path.contains("generated") }
    }
}

dependencies {
    implementation(libs.jackson.annotation)
}

fabrikt {
    generate("aktivitetslogg") {
        apiFile = file("$projectDir/src/main/resources/aktivitetslogg-api.yaml")
        basePackage = "no.nav.dagpenger.aktivitetslogg.api"
        skip = false
        quarkusReflectionConfig = disabled
        typeOverrides {
            datetime = LocalDateTime
        }
        model {
            generate = enabled
            validationLibrary = NoValidation
            extensibleEnums = disabled
            sealedInterfacesForOneOf = enabled
            ignoreUnknownProperties = disabled
            nonNullMapValues = enabled
            serializationLibrary = Jackson
            suffix = "DTO"
        }
    }
}

/*
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
*/
