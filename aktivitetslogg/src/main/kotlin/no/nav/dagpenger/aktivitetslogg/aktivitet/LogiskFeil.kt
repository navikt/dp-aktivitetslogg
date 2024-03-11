package no.nav.dagpenger.aktivitetslogg.aktivitet

import no.nav.dagpenger.aktivitetslogg.AktivitetsloggVisitor
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import java.time.LocalDateTime
import java.util.UUID

class LogiskFeil private constructor(
    id: UUID,
    kontekster: List<SpesifikkKontekst>,
    private val melding: String,
    private val tidsstempel: String = LocalDateTime.now().format(tidsstempelformat),
) : Aktivitet(id, 100, 'S', melding, tidsstempel, kontekster) {
    companion object {
        fun filter(aktiviteter: List<Aktivitet>): List<LogiskFeil> {
            return aktiviteter.filterIsInstance<LogiskFeil>()
        }

        fun opprett(kontekster: List<SpesifikkKontekst>, melding: String) =
            LogiskFeil(UUID.randomUUID(), kontekster, melding)
    }

    override fun accept(visitor: AktivitetsloggVisitor) {
        visitor.visitWarn(id, kontekster, this, melding, tidsstempel)
    }
}