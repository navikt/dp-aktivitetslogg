package no.nav.dagpenger.aktivitetslogg.aktivitet

import no.nav.dagpenger.aktivitetslogg.AktivitetsloggObserver
import no.nav.dagpenger.aktivitetslogg.AktivitetsloggVisitor
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import no.nav.dagpenger.aktivitetslogg.Varselkode
import java.time.LocalDateTime
import java.util.UUID

class FunksjonellFeil private constructor(
    id: UUID,
    kontekster: List<SpesifikkKontekst>,
    private val kode: Varselkode,
    private val melding: String,
    private val tidsstempel: String = LocalDateTime.now().format(tidsstempelformat),
) : Aktivitet(id, 75, 'E', melding, tidsstempel, kontekster) {
    companion object {
        internal fun filter(aktiviteter: List<Aktivitet>): List<FunksjonellFeil> {
            return aktiviteter.filterIsInstance<FunksjonellFeil>()
        }

        internal fun opprett(
            kontekster: List<SpesifikkKontekst>,
            kode: Varselkode,
            melding: String,
        ) = FunksjonellFeil(UUID.randomUUID(), kontekster, kode, melding)
    }

    override fun accept(visitor: AktivitetsloggVisitor) {
        visitor.visitFunksjonellFeil(id, kontekster, this, melding, tidsstempel)
    }

    override fun notify(observer: AktivitetsloggObserver) {
        observer.funksjonellFeil(
            id,
            label,
            kode,
            melding,
            kontekster,
            LocalDateTime.parse(tidsstempel, tidsstempelformat),
        )
    }
}
