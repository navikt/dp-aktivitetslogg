package no.nav.dagpenger.aktivitetslogg.aktivitet

import no.nav.dagpenger.aktivitetslogg.AktivitetsloggObserver
import no.nav.dagpenger.aktivitetslogg.AktivitetsloggVisitor
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import java.time.LocalDateTime
import java.util.UUID

class Varsel private constructor(
    id: UUID,
    kontekster: List<SpesifikkKontekst>,
    private val melding: String,
    private val tidsstempel: String = LocalDateTime.now().format(tidsstempelformat),
) : Aktivitet(id, 25, 'W', melding, tidsstempel, kontekster) {
    companion object {
        internal fun filter(aktiviteter: List<Aktivitet>): List<Varsel> {
            return aktiviteter.filterIsInstance<Varsel>()
        }

        internal fun opprett(
            kontekster: List<SpesifikkKontekst>,
            melding: String,
        ) = Varsel(UUID.randomUUID(), kontekster, melding = melding)
    }

    override fun accept(visitor: AktivitetsloggVisitor) {
        visitor.visitVarsel(id, kontekster, this, melding, tidsstempel)
    }

    override fun notify(observer: AktivitetsloggObserver) {
        observer.varsel(id, label, melding, kontekster, LocalDateTime.parse(tidsstempel, tidsstempelformat))
    }
}
