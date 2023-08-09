package no.nav.dagpenger.aktivitetslogg.mottak

import no.nav.dagpenger.aktivitetslogg.aktivitetslogg.AktivitetsloggRepository
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

internal class AktivitetsloggMottak(
    rapidsConnection: RapidsConnection,
    private val aktivitetsloggRepository: AktivitetsloggRepository,
) : River.PacketListener {

    init {
        River(rapidsConnection).validate {
            it.demandValue("@event_name", "aktivitetslogg")
            it.requireKey("ident")
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        aktivitetsloggRepository.lagre(packet["ident"].asText(), packet.toJson())
    }
}
