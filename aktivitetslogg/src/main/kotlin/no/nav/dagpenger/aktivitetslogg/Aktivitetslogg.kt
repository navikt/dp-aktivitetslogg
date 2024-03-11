package no.nav.dagpenger.aktivitetslogg

import no.nav.dagpenger.aktivitetslogg.aktivitet.Aktivitet
import no.nav.dagpenger.aktivitetslogg.aktivitet.Behov
import no.nav.dagpenger.aktivitetslogg.aktivitet.FunksjonellFeil
import no.nav.dagpenger.aktivitetslogg.aktivitet.Hendelse
import no.nav.dagpenger.aktivitetslogg.aktivitet.Info
import no.nav.dagpenger.aktivitetslogg.aktivitet.LogiskFeil
import no.nav.dagpenger.aktivitetslogg.aktivitet.Varsel

// Understands issues that arose when analyzing a JSON message
// Implements Collecting Parameter in Refactoring by Martin Fowler
// Implements Visitor pattern to traverse the messages
class Aktivitetslogg(
    private var forelder: Aktivitetslogg? = null,
    private val aktiviteter: MutableList<Aktivitet> = mutableListOf(),
) : IAktivitetslogg {
    private val kontekster = mutableListOf<Aktivitetskontekst>()
    private val observers = mutableListOf<AktivitetsloggObserver>()

    override fun info(melding: String) {
        add(Info.opprett(kontekster.toSpesifikk(), melding))
    }

    override fun info(
        melding: String,
        borgerIdent: String,
        saksbehandlerNavIdent: String,
        operasjon: AuditOperasjon,
    ) {
        val kontekst = AuditKontekst(borgerIdent, saksbehandlerNavIdent, AuditKontekst.Alvorlighetsgrad.INFO, operasjon)
        add(Info.opprett((kontekster + kontekst).toSpesifikk(), melding))
    }

    override fun hendelse(
        type: Hendelse.Hendelsetype,
        melding: String,
        detaljer: Map<String, Any>,
    ) {
        add(Hendelse.opprett(type, kontekster.toSpesifikk(), melding, detaljer))
    }

    override fun varsel(
        melding: String,
        borgerIdent: String,
        saksbehandlerNavIdent: String,
        operasjon: AuditOperasjon,
    ) {
        val kontekst = AuditKontekst(borgerIdent, saksbehandlerNavIdent, AuditKontekst.Alvorlighetsgrad.WARN, operasjon)
        add(Varsel.opprett((kontekster + kontekst).toSpesifikk(), melding = melding))
    }

    override fun varsel(melding: String) {
        add(Varsel.opprett(kontekster.toSpesifikk(), melding = melding))
    }

    override fun varsel(kode: Varselkode) {
        add(kode.varsel(kontekster.toSpesifikk()))
    }

    override fun behov(
        type: Behov.Behovtype,
        melding: String,
        detaljer: Map<String, Any>,
    ) {
        add(Behov.opprett(type, kontekster.toSpesifikk(), melding, detaljer))
    }

    override fun funksjonellFeil(kode: Varselkode) {
        TODO("Brukes i kombinasjon med varsel til saksbehandler")
        // add(kode.funksjonellFeil(kontekster.toSpesifikk()))
    }

    override fun logiskFeil(
        melding: String,
        vararg params: Any?,
    ): Nothing {
        add(LogiskFeil.opprett(kontekster.toSpesifikk(), String.format(melding, *params)))

        throw AktivitetException(this)
    }

    override fun harAktiviteter() = info().isNotEmpty() || behov().isNotEmpty()

    override fun harVarslerEllerVerre() = varsel().isNotEmpty() || harFunksjonelleFeilEllerVerre()

    override fun harFunksjonelleFeilEllerVerre() = funksjonelleFeil().isNotEmpty() || logiskFeil().isNotEmpty()

    override fun aktivitetsteller() = aktiviteter.size

    override fun barn() = Aktivitetslogg(this).also { it.kontekster.addAll(this.kontekster) }

    override fun toString() = this.aktiviteter.map { it.inOrder() }.joinToString(separator = "\n") { it }

    private fun add(aktivitet: Aktivitet) {
        observers.forEach { aktivitet.notify(it) }
        this.aktiviteter.add(aktivitet)
        forelder?.add(aktivitet)
    }

    private fun List<Aktivitetskontekst>.toSpesifikk() = this.map { it.toSpesifikkKontekst() }

    override fun kontekst(kontekst: Aktivitetskontekst) {
        val spesifikkKontekst = kontekst.toSpesifikkKontekst()
        val index = kontekster.indexOfFirst { spesifikkKontekst.sammeType(it) }
        if (index >= 0) fjernKonteksterFraOgMed(index)
        kontekster.add(kontekst)
    }

    override fun kontekst(kontekst: Subaktivitetskontekst) {
        forelder = kontekst.aktivitetslogg
        kontekst(kontekst as Aktivitetskontekst)
    }

    private fun fjernKonteksterFraOgMed(indeks: Int) {
        val antall = kontekster.size - indeks
        repeat(antall) { kontekster.removeLast() }
    }

    override fun toMap(mapper: AktivitetsloggMappingPort): Map<String, List<Map<String, Any>>> = mapper.map(this)

    fun logg(vararg kontekst: Aktivitetskontekst): Aktivitetslogg {
        return Aktivitetslogg(this).also {
            it.aktiviteter.addAll(
                this.aktiviteter.filter { aktivitet ->
                    kontekst.any { it in aktivitet }
                },
            )
        }
    }

    internal fun logg(vararg kontekst: String): Aktivitetslogg {
        return Aktivitetslogg(this).also { aktivitetslogg ->
            aktivitetslogg.aktiviteter.addAll(
                this.aktiviteter.filter { aktivitet ->
                    kontekst.any { kontekst -> kontekst in aktivitet.kontekster.map { it.kontekstType } }
                },
            )
        }
    }

    private fun info() = Info.filter(aktiviteter)

    fun varsel() = Varsel.filter(aktiviteter)

    private fun funksjonelleFeil() = FunksjonellFeil.filter(aktiviteter)

    private fun logiskFeil() = LogiskFeil.filter(aktiviteter)

    override fun behov() = Behov.filter(aktiviteter)

    override fun hendelse() = Hendelse.filter(aktiviteter)

    override fun kontekster() =
        aktiviteter
            .groupBy { it.kontekst(null) }
            .map { Aktivitetslogg(this).apply { aktiviteter.addAll(it.value) } }

    override fun accept(visitor: AktivitetsloggVisitor) {
        visitor.preVisitAktivitetslogg(this)
        aktiviteter.forEach { it.accept(visitor) }
        visitor.postVisitAktivitetslogg(this)
    }

    override fun registrer(observer: AktivitetsloggObserver) {
        observers.add(observer)
    }

    class AktivitetException internal constructor(private val aktivitetslogg: Aktivitetslogg) :
        RuntimeException(aktivitetslogg.toString()) {
            fun kontekst() =
                aktivitetslogg.kontekster.fold(mutableMapOf<String, String>()) { result, kontekst ->
                    result.apply { putAll(kontekst.toSpesifikkKontekst().kontekstMap) }
                }

            fun aktivitetslogg() = aktivitetslogg
        }

    companion object {
        fun rehydrer(aktiviteter: List<Aktivitet>) = Aktivitetslogg(forelder = null, aktiviteter = aktiviteter.toMutableList())
    }
}

interface AktivitetsloggMappingPort {
    fun map(log: Aktivitetslogg): Map<String, List<Map<String, Any>>>
}
