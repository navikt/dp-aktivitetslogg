package no.nav.dagpenger.aktivitetslogg

import no.nav.dagpenger.aktivitetslogg.serde.AktivitetsloggJsonBuilder


interface AktivitetsloggMessageContext {
    fun publish(eventNavn: String = "aktivitetslogg", message: Map<String, Any>)
}

interface AktivitetsloggHendelse : Subaktivitetskontekst {
    fun ident(): String
    fun meldingsreferanseId(): String
}


interface IAktivitetsloggMediator : AktivitetsloggMessageContext {
    fun publish(aktivitetsloggHendelse: AktivitetsloggHendelse) {
        publish(
            message = mapOf(
                "hendelse" to mapOf(
                    "type" to aktivitetsloggHendelse.toSpesifikkKontekst().kontekstType,
                    "meldingsreferanseId" to aktivitetsloggHendelse.meldingsreferanseId(),
                ),
                "ident" to aktivitetsloggHendelse.ident(),
                "aktiviteter" to AktivitetsloggJsonBuilder(aktivitetsloggHendelse.aktivitetslogg).asList(),
            )
        )
    }
}