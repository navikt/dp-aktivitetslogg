package no.nav.dagpenger.aktivitetslogg

import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.ContentType
import io.ktor.serialization.jackson3.JacksonConverter
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import kotlinx.coroutines.DelicateCoroutinesApi
import no.nav.dagpenger.aktivitetslogg.aktivitetslogg.PostgresAktivitetsloggRepository
import no.nav.dagpenger.aktivitetslogg.api.aktivitetsloggApi
import no.nav.dagpenger.aktivitetslogg.db.PostgresDataSourceBuilder.dataSource
import no.nav.dagpenger.aktivitetslogg.db.PostgresDataSourceBuilder.runMigration
import no.nav.dagpenger.aktivitetslogg.mottak.AktivitetsloggMottak
import no.nav.dagpenger.aktivitetslogg.serialisering.jacksonObjectMapper
import no.nav.helse.rapids_rivers.RapidApplication

internal class ApplicationBuilder(
    configuration: Map<String, String>,
) : RapidsConnection.StatusListener {
    private val aktivitetsloggRepository = PostgresAktivitetsloggRepository(dataSource)
    private val rapidsConnection: RapidsConnection =
        RapidApplication.create(
            configuration,
            builder = {
                withKtorModule {
                    install(ContentNegotiation) {
                        register(ContentType.Application.Json, JacksonConverter(jacksonObjectMapper))
                    }
                    install(StatusPages) {
                        statusPagesConfig()
                    }
                    aktivitetsloggApi(
                        aktivitetsloggRepository = aktivitetsloggRepository,
                    )
                }
            },
        )

    init {
        rapidsConnection.register(this)
        AktivitetsloggMottak(
            rapidsConnection = rapidsConnection,
            aktivitetsloggRepository,
        )
    }

    fun start() {
        rapidsConnection.start()
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onStartup(rapidsConnection: RapidsConnection) {
        runMigration()
        logger.info { "Starter applikasjonen" }
    }

    override fun onShutdown(rapidsConnection: RapidsConnection) {
        logger.info { "Skrur av applikasjonen" }
    }

    private companion object {
        val logger = KotlinLogging.logger {}
    }
}
