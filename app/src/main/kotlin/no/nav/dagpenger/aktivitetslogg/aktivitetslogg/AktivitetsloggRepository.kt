package no.nav.dagpenger.aktivitetslogg.aktivitetslogg

import kotlinx.coroutines.flow.SharedFlow
import no.nav.dagpenger.aktivitetslogg.api.models.AktivitetsloggDTO
import no.nav.dagpenger.aktivitetslogg.api.models.AntallAktiviteterDTO
import no.nav.dagpenger.aktivitetslogg.api.models.TjenesteDTO
import java.util.UUID

internal interface AktivitetsloggRepository {
    fun hentAktivitetslogg(
        ident: String?,
        tjeneste: String?,
        limit: Int,
        since: UUID? = null,
    ): List<AktivitetsloggDTO>

    fun hentAktivitetslogg(ident: String): List<AktivitetsloggDTO>

    fun lagre(
        uuid: UUID,
        ident: String,
        json: String,
    ): Int

    fun flow(): SharedFlow<List<AktivitetsloggDTO>>

    fun hentTjenester(): List<TjenesteDTO>

    fun antallAktiviteter(): AntallAktiviteterDTO?
}

fun interface AktivitetsloggLytter {
    fun lytt(aktivitetslogg: List<AktivitetsloggDTO>)
}
