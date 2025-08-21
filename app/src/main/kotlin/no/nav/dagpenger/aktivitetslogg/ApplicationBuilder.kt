package no.nav.dagpenger.aktivitetslogg

import com.github.navikt.tbd_libs.naisful.naisApp
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.github.oshai.kotlinlogging.KotlinLogging
import io.micrometer.core.instrument.Clock
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import io.prometheus.metrics.model.registry.PrometheusRegistry
import no.nav.dagpenger.aktivitetslogg.aktivitetslogg.PostgresAktivitetsloggRepository
import no.nav.dagpenger.aktivitetslogg.api.aktivitetsloggApi
import no.nav.dagpenger.aktivitetslogg.crypt.SecretService
import no.nav.dagpenger.aktivitetslogg.db.PostgresDataSourceBuilder.dataSource
import no.nav.dagpenger.aktivitetslogg.db.PostgresDataSourceBuilder.runMigration
import no.nav.dagpenger.aktivitetslogg.mottak.AktivitetsloggMottak
import no.nav.dagpenger.aktivitetslogg.serialisering.jacksonObjectMapper
import no.nav.helse.rapids_rivers.RapidApplication
import org.slf4j.LoggerFactory

internal class ApplicationBuilder(
    configuration: Map<String, String>,
) : RapidsConnection.StatusListener {
    private val aktivitetsloggRepository = PostgresAktivitetsloggRepository(dataSource)
    private val secretService = SecretService()
    private val rapidsConnection: RapidsConnection =
        RapidApplication.create(
            configuration,
            builder = {
                withKtor { preStopHook, rapid ->
                    naisApp(
                        meterRegistry =
                            PrometheusMeterRegistry(
                                PrometheusConfig.DEFAULT,
                                PrometheusRegistry.defaultRegistry,
                                Clock.SYSTEM,
                            ),
                        objectMapper = jacksonObjectMapper,
                        applicationLogger = LoggerFactory.getLogger("ApplicationLogger"),
                        callLogger = LoggerFactory.getLogger("CallLogger"),
                        aliveCheck = rapid::isReady,
                        readyCheck = rapid::isReady,
                    ) {
                        aktivitetsloggApi(
                            aktivitetsloggRepository = aktivitetsloggRepository,
                            secretService = secretService,
                        )
                    }
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
