package no.nav.dagpenger.aktivitetslogg.aktivitetslogg

import com.fasterxml.jackson.module.kotlin.readValue
import kotliquery.Query
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.aktivitetslogg.api.models.AktivitetsloggDTO
import no.nav.dagpenger.aktivitetslogg.serialisering.jacksonObjectMapper
import java.util.UUID
import javax.sql.DataSource

internal class PostgresAktivitetsloggRepository(private val ds: DataSource) : AktivitetsloggRepository {
    override fun hentAktivitetslogg(offset: Int, limit: Int) = hentAktivitetslogg(
        queryOf(
            //language=PostgreSQL
            statement = """SELECT * FROM aktivitetslogg ORDER BY id DESC LIMIT :limit OFFSET :offset""",
            paramMap = mapOf(
                "offset" to offset,
                "limit" to limit,
            ),
        ),
    )

    override fun hentAktivitetslogg(ident: String) = hentAktivitetslogg(
        queryOf(
            //language=PostgreSQL
            statement = """SELECT * FROM aktivitetslogg""",
        ),
    )

    private fun hentAktivitetslogg(query: Query) = using(sessionOf(ds)) { session ->
        session.run(query.map { jacksonObjectMapper.readValue<AktivitetsloggDTO>(it.string("json")) }.asList)
    }

    override fun lagre(uuid: UUID, ident: String, json: String) = using(sessionOf(ds)) { session ->
        session.run(
            queryOf(
                //language=PostgreSQL
                statement = """INSERT INTO aktivitetslogg (melding_id, ident, json) VALUES (:uuid, :ident, :json::jsonb)""",
                paramMap = mapOf(
                    "uuid" to uuid,
                    "ident" to ident,
                    "json" to json,
                ),
            ).asUpdate,
        )
    }
}
