package no.nav.dagpenger.aktivitetslogg

/**
 * @constructor
 * @param borgerIdent Fødselsnummer på borgeren
 * @param saksbehandlerNavIdent NAV-ID på ansatt: A123456. T-ID (abc1234), epost (fornavn.etternavn@nav.no), orgnummer, Fødselsnummer/DNR (for partnere som autentiserer med ID-porten, eller selvbetjening via ID-porten)
 * @param beskrivelse Beskrivelse av hendelsen
 * @param melding En menneskelesbar beskrivelse av hendelsen, tanken er at "average joe" må kunne forstå hva som skjedde
 * @param alvorlighetsgrad Alvorlighetsgraden av hendelsen: INFO, WARN
 * @param operasjon Hvilken operasjon som ble utført: CREATE, READ, UPDATE, DELETE
 */
data class AuditKontekst(
    val borgerIdent: String,
    val saksbehandlerNavIdent: String,
    val alvorlighetsgrad: Alvorlighetsgrad,
    val operasjon: Operasjon
) : Aktivitetskontekst {

    private val appNavn: String = System.getenv("NAIS_APP_NAME") ?: "dagpenger-aktivitetslogg-ukjent"
    enum class Operasjon {
        CREATE,
        READ,
        UPDATE,
        DELETE,
    }

    enum class Alvorlighetsgrad {
        INFO,
        WARN,
    }

    override fun toSpesifikkKontekst(): SpesifikkKontekst {
        return SpesifikkKontekst(
            kontekstType = kontekstType,
            kontekstMap = mapOf(
                "appName" to appNavn,
                "borgerIdent" to borgerIdent,
                "saksbehandlerNavIdent" to saksbehandlerNavIdent,
                "alvorlighetsgrad" to alvorlighetsgrad.name,
                "operasjon" to operasjon.name,
            )
        )
    }

    internal companion object {
        val kontekstType = "audit"
    }
}