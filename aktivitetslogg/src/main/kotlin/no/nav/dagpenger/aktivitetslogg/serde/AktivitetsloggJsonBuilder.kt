package no.nav.dagpenger.aktivitetslogg.serde

import no.nav.dagpenger.aktivitetslogg.AktivitetsloggVisitor
import no.nav.dagpenger.aktivitetslogg.IAktivitetslogg
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import no.nav.dagpenger.aktivitetslogg.Varselkode
import no.nav.dagpenger.aktivitetslogg.aktivitet.Behov
import no.nav.dagpenger.aktivitetslogg.aktivitet.FunksjonellFeil
import no.nav.dagpenger.aktivitetslogg.aktivitet.Hendelse
import no.nav.dagpenger.aktivitetslogg.aktivitet.Info
import no.nav.dagpenger.aktivitetslogg.aktivitet.LogiskFeil
import no.nav.dagpenger.aktivitetslogg.aktivitet.Varsel
import java.util.UUID

class AktivitetsloggJsonBuilder(aktivitetslogg: IAktivitetslogg) : AktivitetsloggVisitor {
    private val aktiviteter = mutableListOf<Map<String, Any>>()

    init {
        aktivitetslogg.accept(this)
    }

    fun asList(): List<Map<String, Any>> {
        return aktiviteter.toList()
    }

    private enum class Alvorlighetsgrad {
        INFO,
        HENDELSE,
        WARN,
        BEHOV,
        ERROR,
        SEVERE,
        AUDIT,
    }

    override fun visitInfo(
        id: UUID,
        kontekster: List<SpesifikkKontekst>,
        aktivitet: Info,
        melding: String,
        tidsstempel: String,
    ) {
        leggTilAktivitet(id, kontekster, Alvorlighetsgrad.INFO, melding, tidsstempel)
    }

    override fun visitHendelse(
        id: UUID,
        kontekster: List<SpesifikkKontekst>,
        aktivitet: Hendelse,
        type: Hendelse.Hendelsetype,
        melding: String,
        detaljer: Map<String, Any?>,
        tidsstempel: String,
    ) {
        leggTilHendelse(
            id,
            kontekster,
            Alvorlighetsgrad.HENDELSE,
            type,
            melding,
            detaljer,
            tidsstempel,
        )
    }

    override fun visitWarn(
        id: UUID,
        kontekster: List<SpesifikkKontekst>,
        aktivitet: LogiskFeil,
        melding: String,
        tidsstempel: String,
    ) {
        leggTilAktivitet(id, kontekster, Alvorlighetsgrad.WARN, melding, tidsstempel)
    }

    override fun visitBehov(
        id: UUID,
        kontekster: List<SpesifikkKontekst>,
        aktivitet: Behov,
        type: Behov.Behovtype,
        melding: String,
        detaljer: Map<String, Any?>,
        tidsstempel: String,
    ) {
        leggTilBehov(
            id,
            kontekster,
            Alvorlighetsgrad.BEHOV,
            type,
            melding,
            detaljer,
            tidsstempel,
        )
    }

    override fun visitVarsel(
        id: UUID,
        kontekster: List<SpesifikkKontekst>,
        varsel: Varsel,
        kode: Varselkode?,
        melding: String,
        tidsstempel: String,
    ) {
        leggTilAktivitet(id, kontekster, Alvorlighetsgrad.ERROR, melding, tidsstempel)
    }

    override fun visitFunksjonellFeil(
        id: UUID,
        kontekster: List<SpesifikkKontekst>,
        funksjonellFeil: FunksjonellFeil,
        melding: String,
        tidsstempel: String,
    ) {
        leggTilAktivitet(id, kontekster, Alvorlighetsgrad.SEVERE, melding, tidsstempel)
    }

    private fun leggTilAktivitet(
        id: UUID,
        kontekster: List<SpesifikkKontekst>,
        alvorlighetsgrad: Alvorlighetsgrad,
        melding: String,
        tidsstempel: String,
    ) {
        aktiviteter.add(
            mutableMapOf(
                "id" to id,
                "kontekster" to map(kontekster),
                "alvorlighetsgrad" to alvorlighetsgrad.name,
                "melding" to melding,
                "detaljer" to emptyMap<String, Any?>(),
                "tidsstempel" to tidsstempel,
            ),
        )
    }

    private fun leggTilHendelse(
        id: UUID,
        kontekster: List<SpesifikkKontekst>,
        alvorlighetsgrad: Alvorlighetsgrad,
        type: Hendelse.Hendelsetype,
        melding: String,
        detaljer: Map<String, Any?>,
        tidsstempel: String,
    ) {
        aktiviteter.add(
            mutableMapOf(
                "id" to id,
                "kontekster" to map(kontekster),
                "alvorlighetsgrad" to alvorlighetsgrad.name,
                "hendelsetype" to type.name,
                "melding" to melding,
                "detaljer" to detaljer,
                "tidsstempel" to tidsstempel,
            ),
        )
    }

    private fun leggTilBehov(
        id: UUID,
        kontekster: List<SpesifikkKontekst>,
        alvorlighetsgrad: Alvorlighetsgrad,
        type: Behov.Behovtype,
        melding: String,
        detaljer: Map<String, Any?>,
        tidsstempel: String,
    ) {
        aktiviteter.add(
            mutableMapOf(
                "id" to id,
                "kontekster" to map(kontekster),
                "alvorlighetsgrad" to alvorlighetsgrad.name,
                "behovtype" to type.name,
                "melding" to melding,
                "detaljer" to detaljer,
                "tidsstempel" to tidsstempel,
            ),
        )
    }

    private fun map(kontekster: List<SpesifikkKontekst>) =
        kontekster.map {
            mutableMapOf(
                "kontekstType" to it.kontekstType,
                "kontekstMap" to it.kontekstMap,
            )
        }
}
