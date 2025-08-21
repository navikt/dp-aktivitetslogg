package no.nav.dagpenger.aktivitetslogg

import no.nav.dagpenger.aktivitetslogg.aktivitet.Varsel

abstract class Varselkode(private val varseltekst: String) {
    internal fun varsel(kontekster: List<SpesifikkKontekst>): Varsel = Varsel.opprett(kontekster, this, varseltekst)

    override fun toString() = "${this::class.java.simpleName}: $varseltekst"
}
