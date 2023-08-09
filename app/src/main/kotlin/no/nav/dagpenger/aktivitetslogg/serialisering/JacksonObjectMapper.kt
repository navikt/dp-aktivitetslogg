package no.nav.dagpenger.aktivitetslogg.serialisering

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

fun ObjectMapper.configureJackson() {
    registerModule(JavaTimeModule())
    registerModule(
        KotlinModule.Builder().apply {
            enable(KotlinFeature.NullToEmptyCollection)
            enable(KotlinFeature.NullToEmptyMap)
            enable(KotlinFeature.NullIsSameAsDefault)
            enable(KotlinFeature.SingletonSupport)
            enable(KotlinFeature.StrictNullChecks)
        }.build(),
    )
    disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    enable(SerializationFeature.INDENT_OUTPUT)
}

val jacksonObjectMapper by lazy { jacksonObjectMapper().apply { configureJackson() } }
