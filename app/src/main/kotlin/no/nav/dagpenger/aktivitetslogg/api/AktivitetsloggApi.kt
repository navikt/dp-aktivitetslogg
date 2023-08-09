package no.nav.dagpenger.aktivitetslogg.api

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.request.path
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import no.nav.dagpenger.aktivitetslogg.aktivitetslogg.AktivitetsloggRepository
import no.nav.dagpenger.aktivitetslogg.api.auth.AzureAd
import no.nav.dagpenger.aktivitetslogg.api.auth.verifier
import no.nav.dagpenger.aktivitetslogg.serialisering.jacksonObjectMapper
import org.slf4j.event.Level

internal fun Application.aktivitetsloggApi(
    aktivitetsloggRepository: AktivitetsloggRepository,
) {
    install(CallLogging) {
        disableDefaultColors()
        filter {
            it.request.path() !in setOf("/metrics", "/isalive", "/isready")
        }
        level = Level.INFO
    }
    install(ContentNegotiation) {
        jackson {
        }
    }

    install(Authentication) {
        jwt("azureAd") {
            verifier(AzureAd)
            validate { credentials ->
                JWTPrincipal(credentials.payload)
            }
        }
    }

    routing {
        swaggerUI(path = "openapi", swaggerFile = "aktivitetslogg-api.yaml")

        authenticate("azureAd") {
            route("/aktivitetslogg") {
                get {
                    val rapporteringsperioder = aktivitetsloggRepository.hentAktivitetslogg("ident")

                    call.respond(HttpStatusCode.OK, rapporteringsperioder)
                }
            }
        }
    }
}
