package no.nav.dagpenger.aktivitetslogg.aktivitetslogg

import no.nav.dagpenger.aktivitetslogg.api.models.AktivitetsloggDTO
import java.math.BigInteger
import java.util.UUID

internal interface AktivitetsloggRepository {
    fun hentAktivitetslogg(limit: Int, since: UUID? = null): List<AktivitetsloggDTO>
    fun hentAktivitetslogg(ident: String): List<AktivitetsloggDTO>
    fun lagre(uuid: UUID, ident: String, json: String): Int
}
