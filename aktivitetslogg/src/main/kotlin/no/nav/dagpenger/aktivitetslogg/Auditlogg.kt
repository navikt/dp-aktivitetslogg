package no.nav.dagpenger.aktivitetslogg

enum class Alvorlighetsgrad {
    INFO,
    WARN,
}

data class AuditLogg(
    val ident: String,
    val saksbehandlerIdent: String,
    val beskrivelse: String,
    val alvorlighetsgrad: Alvorlighetsgrad,
)
