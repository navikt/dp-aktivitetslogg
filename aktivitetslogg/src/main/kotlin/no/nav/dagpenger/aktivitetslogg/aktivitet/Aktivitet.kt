package no.nav.dagpenger.aktivitetslogg.aktivitet

import no.nav.dagpenger.aktivitetslogg.Aktivitetskontekst
import no.nav.dagpenger.aktivitetslogg.AktivitetsloggObserver
import no.nav.dagpenger.aktivitetslogg.AktivitetsloggVisitor
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

// Representerer aktivitet i en aktivitetskontekst som logges
sealed class Aktivitet(
    protected val id: UUID,
    private val alvorlighetsgrad: Int,
    protected val label: Char,
    private var melding: String,
    private val tidsstempel: String,
    val kontekster: List<SpesifikkKontekst>,
) : Comparable<Aktivitet> {
    internal companion object {
        internal val tidsstempelformat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
    }

    fun kontekst(): Map<String, String> = kontekst(null)

    internal fun kontekst(typer: Array<String>?): Map<String, String> = kontekster
        .filter { typer == null || it.kontekstType in typer }
        .fold(mapOf()) { result, kontekst -> result + kontekst.kontekstMap }

    override fun compareTo(other: Aktivitet) = this.tidsstempel.compareTo(other.tidsstempel)
        .let { if (it == 0) other.alvorlighetsgrad.compareTo(this.alvorlighetsgrad) else it }

    internal fun inOrder() = label + "\t" + this.toString()

    override fun toString() = label + "  \t" + tidsstempel + "  \t" + melding + meldingerString()

    private fun meldingerString(): String {
        return kontekster.joinToString(separator = "") { " (${it.melding()})" }
    }

    internal abstract fun accept(visitor: AktivitetsloggVisitor)

    internal open fun notify(observer: AktivitetsloggObserver) {
        observer.aktivitet(id, label, melding, kontekster, LocalDateTime.parse(tidsstempel, tidsstempelformat))
    }

    operator fun contains(kontekst: Aktivitetskontekst) = kontekst.toSpesifikkKontekst() in kontekster
}

