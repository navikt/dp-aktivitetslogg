package no.nav.dagpenger.aktivitetslogg.aktivitetslogg

import kotlinx.coroutines.flow.SharedFlow
import no.nav.dagpenger.aktivitetslogg.api.models.AktivitetsloggDTO
import java.util.UUID

internal interface AktivitetsloggRepository {
    fun hentAktivitetslogg(limit: Int, since: UUID? = null): List<AktivitetsloggDTO>
    fun hentAktivitetslogg(ident: String): List<AktivitetsloggDTO>
    fun lagre(uuid: UUID, ident: String, json: String): Int
    fun flow(): SharedFlow<List<AktivitetsloggDTO>>
}

fun interface AktivitetsloggLytter {
    fun lytt(aktivitetslogg: List<AktivitetsloggDTO>)
}
