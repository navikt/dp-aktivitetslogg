package no.nav.dagpenger.aktivitetslogg.aktivitetslogg

import no.nav.dagpenger.aktivitetslogg.api.models.AktivitetsloggDTO
import java.util.UUID

internal interface AktivitetsloggRepository {
    fun hentAktivitetslogg(offset: Int, limit: Int): List<AktivitetsloggDTO>
    fun hentAktivitetslogg(ident: String): List<AktivitetsloggDTO>
    fun lagre(uuid: UUID, ident: String, json: String): Int
}
