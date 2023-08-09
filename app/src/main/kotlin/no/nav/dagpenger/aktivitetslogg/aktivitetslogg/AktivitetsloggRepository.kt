package no.nav.dagpenger.aktivitetslogg.aktivitetslogg

import no.nav.dagpenger.aktivitetslogg.api.models.AktivitetsloggDTO

internal interface AktivitetsloggRepository {
    fun hentAktivitetslogg(ident: String): List<AktivitetsloggDTO>
    fun lagre(ident: String, json: String): Int
}
