package no.nav.dagpenger.aktivitetslogg.mottak

import com.fasterxml.jackson.databind.JsonNode
import no.nav.dagpenger.aktivitetslogg.aktivitetslogg.AktivitetsloggRepository
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import java.util.UUID

internal class AktivitetsloggMottak(
    rapidsConnection: RapidsConnection,
    private val aktivitetsloggRepository: AktivitetsloggRepository,
) : River.PacketListener {
    init {
        River(rapidsConnection).validate {
            it.demandValue("@event_name", "aktivitetslogg")
            it.requireKey("@id", "ident")
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        aktivitetsloggRepository.lagre(packet["@id"].asUUID(), packet["ident"].asText(), packet.toJson())
    }
}

private fun JsonNode.asUUID() = UUID.fromString(asText())
