package no.nav.dagpenger.aktivitetslogg.aktivitet

import no.nav.dagpenger.aktivitetslogg.AktivitetsloggVisitor
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import java.time.LocalDateTime
import java.util.UUID

class Behov private constructor(
    id: UUID,
    val type: Behovtype,
    kontekster: List<SpesifikkKontekst>,
    private val melding: String,
    private val detaljer: Map<String, Any> = emptyMap(),
    private val tidsstempel: String = LocalDateTime.now().format(tidsstempelformat),
) : Aktivitet(id, 50, 'N', melding, tidsstempel, kontekster) {
    companion object {
        fun filter(aktiviteter: List<Aktivitet>): List<Behov> {
            return aktiviteter.filterIsInstance<Behov>()
        }

        fun opprett(
            type: Behovtype,
            kontekster: List<SpesifikkKontekst>,
            melding: String,
            detaljer: Map<String, Any>,
        ) = Behov(
            UUID.randomUUID(),
            type,
            kontekster,
            melding,
            detaljer,
        )
    }

    fun detaljer() = detaljer

    override fun accept(visitor: AktivitetsloggVisitor) {
        visitor.visitBehov(id, kontekster, this, type, melding, detaljer, tidsstempel)
    }

    interface Behovtype {
        val name: String
    }
}
