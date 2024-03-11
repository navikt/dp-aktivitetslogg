package no.nav.dagpenger.aktivitetslogg.aktivitet

import no.nav.dagpenger.aktivitetslogg.AktivitetsloggVisitor
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import java.time.LocalDateTime
import java.util.UUID

class Info private constructor(
    id: UUID,
    kontekster: List<SpesifikkKontekst>,
    private val melding: String,
    private val tidsstempel: String = LocalDateTime.now().format(tidsstempelformat),
) : Aktivitet(id, 0, 'I', melding, tidsstempel, kontekster) {
    companion object {
        fun filter(aktiviteter: List<Aktivitet>): List<Info> {
            return aktiviteter.filterIsInstance<Info>()
        }

        fun opprett(kontekster: List<SpesifikkKontekst>, melding: String) =
            Info(UUID.randomUUID(), kontekster, melding)
    }

    override fun accept(visitor: AktivitetsloggVisitor) {
        visitor.visitInfo(id, kontekster, this, melding, tidsstempel)
    }
}