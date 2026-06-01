package no.nav.dagpenger.aktivitetslogg.mottak

import com.fasterxml.jackson.databind.JsonNode
import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.MeterRegistry
import no.nav.dagpenger.aktivitetslogg.aktivitetslogg.AktivitetsloggRepository
import java.util.UUID

internal class AktivitetsloggMottak(
    rapidsConnection: RapidsConnection,
    private val aktivitetsloggRepository: AktivitetsloggRepository,
) : River.PacketListener {
    init {
        River(rapidsConnection)
            .validate {
                it.requireValue("@event_name", "aktivitetslogg")
                it.requireKey("@id", "ident")
            }.register(this)
    }

    private val skipList = listOf(UUID.fromString("e20d616b-0d83-4c89-9942-3319a9177c6f"))

    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
        metadata: MessageMetadata,
        meterRegistry: MeterRegistry,
    ) {
        if (!packet["ident"].asText().matches(Regex("[0-9]{11}"))) {
            logger.warn { "Mottok aktivitetslogg med ugyldig ident" }
            return
        }

        val meldingId = packet["@id"].asUUID()
        if (meldingId in skipList) {
            logger.warn { "Hopper over melding i skipList" }
            return
        }

        aktivitetsloggRepository.lagre(meldingId, packet["ident"].asText(), packet.toJson())
    }

    private companion object {
        val logger =
            io.github.oshai.kotlinlogging.KotlinLogging
                .logger {}
    }
}

private fun JsonNode.asUUID() = UUID.fromString(asText())
