package no.nav.dagpenger.aktivitetslogg

import no.nav.dagpenger.aktivitetslogg.aktivitet.Hendelse
import java.time.LocalDateTime
import java.util.UUID

// Observer for å følge med på en aktivitetslogg
// Kan for eksempel brukes til å publisere hendelser på Kafka
// https://refactoring.guru/design-patterns/observer
interface AktivitetsloggObserver {
    fun aktivitet(
        id: UUID,
        label: Char,
        melding: String,
        kontekster: List<SpesifikkKontekst>,
        tidsstempel: LocalDateTime,
    ) {
    }

    fun hendelse(
        id: UUID,
        label: Char,
        type: Hendelse.Hendelsetype,
        melding: String,
        kontekster: List<SpesifikkKontekst>,
        tidsstempel: LocalDateTime,
    ) {
    }

    fun varsel(
        id: UUID,
        label: Char,
        kode: Varselkode?,
        melding: String,
        kontekster: List<SpesifikkKontekst>,
        tidsstempel: LocalDateTime,
    ) {
    }

    fun funksjonellFeil(
        id: UUID,
        label: Char,
        kode: Varselkode,
        melding: String,
        kontekster: List<SpesifikkKontekst>,
        tidsstempel: LocalDateTime,
    ) {
    }
}
