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

        aktivitetsloggRepository.lagre(packet["@id"].asUUID(), packet["ident"].asText(), packet.toJson())
    }

    private companion object {
        val logger =
            io.github.oshai.kotlinlogging.KotlinLogging
                .logger {}
    }
}

private fun JsonNode.asUUID() = UUID.fromString(asText())
