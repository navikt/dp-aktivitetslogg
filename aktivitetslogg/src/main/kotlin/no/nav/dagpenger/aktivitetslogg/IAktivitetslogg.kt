package no.nav.dagpenger.aktivitetslogg

import no.nav.dagpenger.aktivitetslogg.aktivitet.Behov
import no.nav.dagpenger.aktivitetslogg.aktivitet.Hendelse

interface IAktivitetslogg {
    fun info(melding: String)

    fun info(
        melding: String,
        borgerIdent: String,
        saksbehandlerNavIdent: String,
        operasjon: AuditOperasjon,
    )

    fun hendelse(
        type: Hendelse.Hendelsetype,
        melding: String,
        detaljer: Map<String, Any> = emptyMap(),
    )

    fun behov(
        type: Behov.Behovtype,
        melding: String,
        detaljer: Map<String, Any> = emptyMap(),
    )

    fun varsel(melding: String)

    fun varsel(
        melding: String,
        borgerIdent: String,
        saksbehandlerNavIdent: String,
        operasjon: AuditOperasjon,
    )

    fun varsel(kode: Varselkode)

    fun funksjonellFeil(kode: Varselkode)

    fun logiskFeil(
        melding: String,
        vararg params: Any?,
    ): Nothing

    fun harAktiviteter(): Boolean

    fun harVarslerEllerVerre(): Boolean

    fun harFunksjonelleFeilEllerVerre(): Boolean

    fun aktivitetsteller(): Int

    fun hendelse(): List<Hendelse>

    fun behov(): List<Behov>

    fun barn(): IAktivitetslogg

    fun kontekst(kontekst: Aktivitetskontekst)

    fun kontekst(kontekst: Subaktivitetskontekst)

    fun kontekster(): List<IAktivitetslogg>

    fun toMap(mapper: AktivitetsloggMappingPort): Map<String, List<Map<String, Any>>>

    fun accept(visitor: AktivitetsloggVisitor)

    fun registrer(observer: AktivitetsloggObserver)
}
