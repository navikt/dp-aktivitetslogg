package no.nav.dagpenger.aktivitetslogg.aktivitetslogg

import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotliquery.Query
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import mu.KotlinLogging
import no.nav.dagpenger.aktivitetslogg.api.models.AktivitetsloggDTO
import no.nav.dagpenger.aktivitetslogg.api.models.ServiceDTO
import no.nav.dagpenger.aktivitetslogg.serialisering.jacksonObjectMapper
import java.util.UUID
import javax.sql.DataSource

private val logger = KotlinLogging.logger {}

internal class PostgresAktivitetsloggRepository(
    private val ds: DataSource,
) : AktivitetsloggRepository {
    private val messageSharedFlow = MutableSharedFlow<List<AktivitetsloggDTO>>()

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
        logger.info { "Annonserer ny aktivitetslogg med id=$uuid" }
        GlobalScope.launch {
            val emitted = messageSharedFlow.emit(listOf(aktivitetsloggDTO(json)))
            logger.info { "tryEmit as $emitted" }
        }
    }

    override fun flow() = messageSharedFlow.asSharedFlow()
    override fun hentTjenester(): List<ServiceDTO> = using(sessionOf(ds)) {
       session -> session.run(queryOf(
            //language=PostgreSQL
            statement = """
                select distinct 
                    jsonb_array_elements(json->'system_participating_services')->>'service' as name, 
                    jsonb_array_elements(json->'system_participating_services')->>'instance' as instance 
                from aktivitetslogg
            """.trimIndent()
        ).map { row -> ServiceDTO(name = row.string("name"), instance = row.string("instance")) }.asList)
    }
}
