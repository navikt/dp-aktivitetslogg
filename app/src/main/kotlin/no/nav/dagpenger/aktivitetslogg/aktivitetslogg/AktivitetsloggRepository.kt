package no.nav.dagpenger.aktivitetslogg.aktivitetslogg

import kotlinx.coroutines.flow.SharedFlow
import no.nav.dagpenger.aktivitetslogg.api.models.AktivitetsloggDTO
import no.nav.dagpenger.aktivitetslogg.api.models.AntallAktiviteterDTO
import java.util.UUID

internal interface AktivitetsloggRepository {
    fun hentAktivitetslogg(
        ident: String?,
        limit: Int,
        since: UUID? = null,
    ): List<AktivitetsloggDTO>

    fun hentForKontekst(
        kontekstType: String,
        kontekstVerdi: String,
    ): List<AktivitetsloggDTO>

    fun hentAktivitetslogg(ident: String): List<AktivitetsloggDTO>

    fun lagre(
        uuid: UUID,
        ident: String,
        json: String,
    ): Int

    fun flow(): SharedFlow<List<AktivitetsloggDTO>>

    fun antallAktiviteter(): AntallAktiviteterDTO?
}

fun interface AktivitetsloggLytter {
    fun lytt(aktivitetslogg: List<AktivitetsloggDTO>)
}
