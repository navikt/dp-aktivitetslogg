package no.nav.dagpenger.aktivitetslogg.serialisering

import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.SerializationFeature
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinFeature
import tools.jackson.module.kotlin.KotlinModule

val jacksonObjectMapper by lazy {
    JsonMapper
        .builder()
        .addModule(
            KotlinModule
                .Builder()
                .apply {
                    enable(KotlinFeature.NullToEmptyCollection)
                    enable(KotlinFeature.NullToEmptyMap)
                    enable(KotlinFeature.NullIsSameAsDefault)
                    enable(KotlinFeature.SingletonSupport)
                    enable(KotlinFeature.StrictNullChecks)
                }.build(),
        ).enable(SerializationFeature.INDENT_OUTPUT)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .build()
}
