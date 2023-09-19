package no.nav.dagpenger.aktivitetslogg

import no.nav.dagpenger.aktivitetslogg.serde.AktivitetsloggJsonBuilder

interface PersonHendelse : Aktivitetskontekst, IAktivitetslogg {
    fun ident(): String
    fun meldingsreferanseId(): String
}

interface IAktivitetsloggMediator {
    fun håndter(personHendelse: PersonHendelse, publish: (AktivitetsLoggMelding) -> Unit) {
        publish(
            AktivitetsLoggMelding(
                mapOf(
                    "hendelse" to mapOf(
                        "type" to personHendelse.toSpesifikkKontekst().kontekstType,
                        "meldingsreferanseId" to personHendelse.meldingsreferanseId(),
                    ),
                    "ident" to personHendelse.ident(),
                    "aktiviteter" to AktivitetsloggJsonBuilder(personHendelse).asList(),
                )
            )
        )
    }
    data class AktivitetsLoggMelding(val innhold: Map<String, Any>) {
        val eventNavn: String = "aktivitetslogg"
    }
}