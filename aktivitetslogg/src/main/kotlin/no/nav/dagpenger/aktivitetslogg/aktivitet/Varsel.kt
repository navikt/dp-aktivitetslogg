package no.nav.dagpenger.aktivitetslogg.aktivitet

import no.nav.dagpenger.aktivitetslogg.AktivitetsloggObserver
import no.nav.dagpenger.aktivitetslogg.AktivitetsloggVisitor
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import no.nav.dagpenger.aktivitetslogg.Varselkode
import java.time.LocalDateTime
import java.util.UUID

class Varsel private constructor(
    id: UUID,
    kontekster: List<SpesifikkKontekst>,
    private val kode: Varselkode? = null,
    private val melding: String,
    private val tidsstempel: String = LocalDateTime.now().format(tidsstempelformat),
) : Aktivitet(id, 25, 'W', melding, tidsstempel, kontekster) {
    companion object {
        internal fun filter(aktiviteter: List<Aktivitet>): List<Varsel> {
            return aktiviteter.filterIsInstance<Varsel>()
        }

        internal fun opprett(
            kontekster: List<SpesifikkKontekst>,
            kode: Varselkode? = null,
            melding: String,
        ) = Varsel(UUID.randomUUID(), kontekster, kode, melding = melding)
    }

    override fun accept(visitor: AktivitetsloggVisitor) {
        visitor.visitVarsel(id, kontekster, this, kode, melding, tidsstempel)
    }

    override fun notify(observer: AktivitetsloggObserver) {
        observer.varsel(id, label, kode, melding, kontekster, LocalDateTime.parse(tidsstempel, tidsstempelformat))
    }
}
