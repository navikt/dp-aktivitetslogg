package no.nav.dagpenger.aktivitetslogg

import no.nav.dagpenger.aktivitetslogg.aktivitet.Behov
import no.nav.dagpenger.aktivitetslogg.aktivitet.FunksjonellFeil
import no.nav.dagpenger.aktivitetslogg.aktivitet.Hendelse
import no.nav.dagpenger.aktivitetslogg.aktivitet.Info
import no.nav.dagpenger.aktivitetslogg.aktivitet.LogiskFeil
import no.nav.dagpenger.aktivitetslogg.aktivitet.Varsel
import java.util.UUID

// Visitor for å besøke en aktivitetslogg.
// Kan for eksempel brukes til å serialisere den til JSON
// https://refactoring.guru/design-patterns/visitor
interface AktivitetsloggVisitor {
    fun preVisitAktivitetslogg(aktivitetslogg: Aktivitetslogg) {}

    fun visitInfo(
        id: UUID,
        kontekster: List<SpesifikkKontekst>,
        aktivitet: Info,
        melding: String,
        tidsstempel: String,
    ) {
    }

    fun visitWarn(
        id: UUID,
        kontekster: List<SpesifikkKontekst>,
        aktivitet: LogiskFeil,
        melding: String,
        tidsstempel: String,
    ) {
    }

    fun visitHendelse(
        id: UUID,
        kontekster: List<SpesifikkKontekst>,
        aktivitet: Hendelse,
        type: Hendelse.Hendelsetype,
        melding: String,
        detaljer: Map<String, Any?>,
        tidsstempel: String,
    ) {
    }

    fun visitBehov(
        id: UUID,
        kontekster: List<SpesifikkKontekst>,
        aktivitet: Behov,
        type: Behov.Behovtype,
        melding: String,
        detaljer: Map<String, Any?>,
        tidsstempel: String,
    ) {
    }

    fun visitVarsel(
        id: UUID,
        kontekster: List<SpesifikkKontekst>,
        varsel: Varsel,
        kode: Varselkode?,
        melding: String,
        tidsstempel: String,
    ) {
    }

    fun visitFunksjonellFeil(
        id: UUID,
        kontekster: List<SpesifikkKontekst>,
        funksjonellFeil: FunksjonellFeil,
        melding: String,
        tidsstempel: String,
    ) {
    }

    fun postVisitAktivitetslogg(aktivitetslogg: Aktivitetslogg) {}
}
