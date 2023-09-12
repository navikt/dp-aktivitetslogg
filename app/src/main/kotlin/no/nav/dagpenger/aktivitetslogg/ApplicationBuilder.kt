package no.nav.dagpenger.aktivitetslogg

import mu.KotlinLogging
import no.nav.dagpenger.aktivitetslogg.aktivitetslogg.PostgresAktivitetsloggRepository
import no.nav.dagpenger.aktivitetslogg.api.aktivitetsloggApi
import no.nav.dagpenger.aktivitetslogg.crypt.SecretService
import no.nav.dagpenger.aktivitetslogg.db.PostgresDataSourceBuilder.dataSource
import no.nav.dagpenger.aktivitetslogg.db.PostgresDataSourceBuilder.runMigration
import no.nav.dagpenger.aktivitetslogg.mottak.AktivitetsloggMottak
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection

internal class ApplicationBuilder(configuration: Map<String, String>) : RapidsConnection.StatusListener {
    private val aktivitetsloggRepository = PostgresAktivitetsloggRepository(dataSource)
    private val secretService = SecretService()
    private val rapidsConnection: RapidsConnection =
        RapidApplication.Builder(RapidApplication.RapidApplicationConfig.fromEnv(configuration))
            .withKtorModule {
                aktivitetsloggApi(aktivitetsloggRepository, secretService)
            }.build()

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
