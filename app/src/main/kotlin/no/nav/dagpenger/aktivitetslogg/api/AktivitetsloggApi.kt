package no.nav.dagpenger.aktivitetslogg.api

import com.github.navikt.tbd_libs.rapids_and_rivers.toUUID
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.request.path
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import io.ktor.server.response.respondTextWriter
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import mu.KotlinLogging
import no.nav.dagpenger.aktivitetslogg.aktivitetslogg.AktivitetsloggRepository
import no.nav.dagpenger.aktivitetslogg.api.auth.AzureAd
import no.nav.dagpenger.aktivitetslogg.api.auth.verifier
import no.nav.dagpenger.aktivitetslogg.api.models.AntallAktiviteterDTO
import no.nav.dagpenger.aktivitetslogg.api.models.KeysDTO
import no.nav.dagpenger.aktivitetslogg.crypt.SecretService
import no.nav.dagpenger.aktivitetslogg.crypt.toDecryptedStringOrNull
import no.nav.dagpenger.aktivitetslogg.serialisering.configureJackson
import no.nav.dagpenger.aktivitetslogg.serialisering.jacksonObjectMapper
import no.nav.dagpenger.aktivitetslogg.toStringOrNull
import org.slf4j.event.Level

private val logger = KotlinLogging.logger {}
private val sikkerLogger = KotlinLogging.logger("tjenestekall")

internal fun Application.aktivitetsloggApi(
    aktivitetsloggRepository: AktivitetsloggRepository,
    secretService: SecretService,
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
            configureJackson()
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
                    logger.info { call.request.uri }
                    val params = call.request.queryParameters
                    val limit = params["limit"]?.toIntOrNull() ?: 50
                    val since = params["since"]?.toUUID()
                    val wait = params["wait"]?.toBooleanStrict()
                    val ident = params["ident"]?.toDecryptedStringOrNull(secretService.privateKey())
                    val tjeneste = params["tjeneste"]?.toStringOrNull()

                    sikkerLogger.info { "Ident=$ident" }

                    val aktivitetslogger =
                        aktivitetsloggRepository.hentAktivitetslogg(ident, tjeneste, limit = limit, since = since)
                    if (aktivitetslogger.isEmpty() && wait == true) {
                        val flyt = aktivitetsloggRepository.flow()
                        call.respondTextWriter(contentType = ContentType.Application.Json) {
                            flyt.collect { value ->
                                write(value.toJson())
                                close()
                            }
                        }
                    } else {
                        call.respond(HttpStatusCode.OK, aktivitetslogger)
                    }
                }
                get("tjenester") {
                    call.respond(HttpStatusCode.OK, aktivitetsloggRepository.hentTjenester())
                }
                get("antall") {
                    call.respond(
                        HttpStatusCode.OK,
                        aktivitetsloggRepository.antallAktiviteter() ?: AntallAktiviteterDTO(0),
                    )
                }
                get("keys") {
                    call.respond(
                        HttpStatusCode.OK,
                        KeysDTO(public = secretService.publicKeyAsString()),
                    )
                }
            }
        }
    }
}

private fun <E> List<E>.toJson() = jacksonObjectMapper.writeValueAsString(this)
