package no.nav.dagpenger.aktivitetslogg.aktivitetslogg

import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.channels.Channel
import kotliquery.Query
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.aktivitetslogg.api.models.AktivitetsloggDTO
import no.nav.dagpenger.aktivitetslogg.serialisering.jacksonObjectMapper
import java.util.UUID
import javax.sql.DataSource

internal class PostgresAktivitetsloggRepository(
    private val ds: DataSource,
) : AktivitetsloggRepository {
    private val observers = Channel<List<AktivitetsloggDTO>>()

    override fun hentAktivitetslogg(limit: Int, since: UUID?) = hentAktivitetslogg(
        queryOf(
            //language=PostgreSQL
            statement = """
                SELECT *
                FROM aktivitetslogg
                WHERE (:since::uuid IS NULL OR id > COALESCE((SELECT id FROM aktivitetslogg WHERE melding_id = :since), id))
                ORDER BY id DESC
                LIMIT :limit
            """.trimIndent(),
            paramMap = mapOf(
                "since" to since,
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
        session.run(query.map { aktivitetsloggDTO(it.string("json")) }.asList)
    }

    private fun aktivitetsloggDTO(json: String) = jacksonObjectMapper.readValue<AktivitetsloggDTO>(json)

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
    }.also {
        observers.trySend(listOf(aktivitetsloggDTO(json)))
    }

    override suspend fun lytt(): List<AktivitetsloggDTO> = observers.receive()
}
