package no.nav.dagpenger.aktivitetslogg

import no.nav.dagpenger.aktivitetslogg.serde.AktivitetsloggJsonBuilder
import java.util.UUID

interface AktivitetsloggHendelse  : Aktivitetskontekst, IAktivitetslogg {
    fun ident(): String
    fun meldingsreferanseId(): UUID
}

class AktivitetsloggEventMapper {
    fun håndter(personHendelse: AktivitetsloggHendelse, publish: (AktivitetsloggMelding) -> Unit) {
        publish(
            AktivitetsloggMelding(
                mapOf(
                    "hendelse" to mapOf(
                        "type" to personHendelse.toSpesifikkKontekst().kontekstType,
                        "meldingsreferanseId" to personHendelse.meldingsreferanseId().toString(),
                    ),
                    "ident" to personHendelse.ident(),
                    "aktiviteter" to AktivitetsloggJsonBuilder(personHendelse).asList(),
                )
            )
        )
    }
    data class AktivitetsloggMelding(val innhold: Map<String, Any>) {
        val eventNavn: String = "aktivitetslogg"
    }
}