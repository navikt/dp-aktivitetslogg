package no.nav.dagpenger.aktivitetslogg.aktivitet

import no.nav.dagpenger.aktivitetslogg.AktivitetsloggObserver
import no.nav.dagpenger.aktivitetslogg.AktivitetsloggVisitor
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import java.time.LocalDateTime
import java.util.UUID

class Hendelse private constructor(
    id: UUID,
    val type: Hendelsetype,
    kontekster: List<SpesifikkKontekst>,
    private val melding: String,
    private val detaljer: Map<String, Any> = emptyMap(),
    private val tidsstempel: String = LocalDateTime.now().format(tidsstempelformat),
) : Aktivitet(id, 10, 'H', melding, tidsstempel, kontekster) {
    companion object {
        fun filter(aktiviteter: List<Aktivitet>): List<Hendelse> {
            return aktiviteter.filterIsInstance<Hendelse>()
        }

        fun opprett(
            type: Hendelsetype,
            kontekster: List<SpesifikkKontekst>,
            melding: String,
            detaljer: Map<String, Any>,
        ) = Hendelse(
            UUID.randomUUID(),
            type,
            kontekster,
            melding,
            detaljer,
        )
    }

    fun detaljer() = detaljer

    override fun accept(visitor: AktivitetsloggVisitor) {
        visitor.visitHendelse(id, kontekster, this, type, melding, detaljer, tidsstempel)
    }

    override fun notify(observer: AktivitetsloggObserver) {
        observer.hendelse(id, label, type, melding, kontekster, LocalDateTime.parse(tidsstempel, tidsstempelformat))
    }

    interface Hendelsetype {
        val name: String
    }
}
