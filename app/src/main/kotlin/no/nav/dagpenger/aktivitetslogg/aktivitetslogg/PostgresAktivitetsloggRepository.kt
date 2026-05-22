package no.nav.dagpenger.aktivitetslogg.aktivitetslogg

import com.fasterxml.jackson.module.kotlin.readValue
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotliquery.Query
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.aktivitetslogg.api.models.AktivitetsloggDTO
import no.nav.dagpenger.aktivitetslogg.api.models.AntallAktiviteterDTO
import no.nav.dagpenger.aktivitetslogg.api.models.TjenesteDTO
import no.nav.dagpenger.aktivitetslogg.serialisering.jacksonObjectMapper
import java.util.UUID
import javax.sql.DataSource

private val logger = KotlinLogging.logger {}

internal class PostgresAktivitetsloggRepository(
    private val ds: DataSource,
) : AktivitetsloggRepository {
    private val messageSharedFlow = MutableSharedFlow<List<AktivitetsloggDTO>>()

    override fun hentAktivitetslogg(
        ident: String?,
        tjeneste: String?,
        limit: Int,
        since: UUID?,
    ) = hentAktivitetslogg(
        queryOf(
            //language=PostgreSQL
            statement =
                """
                SELECT *
                FROM aktivitetslogg
                WHERE (:since::uuid IS NULL OR id > COALESCE((SELECT id FROM aktivitetslogg WHERE melding_id = :since), id))
                AND (:ident::text IS NULL OR ident = :ident)
                AND (:tjeneste::text IS NULL OR json->'system_participating_services' @> '[{"service": "$tjeneste"}]' )
                ORDER BY id DESC
                LIMIT :limit
                """.trimIndent(),
            paramMap =
                mapOf(
                    "ident" to ident,
                    "tjeneste" to tjeneste,
                    "since" to since,
                    "limit" to limit,
                ),
        ),
    )

    override fun hentForKontekst(
        kontekstType: String,
        kontekstVerdi: String,
    ) = hentAktivitetslogg(
        queryOf(
            //language=PostgreSQL
            statement =
                """
                SELECT a.*
                FROM aktivitetslogg a
                JOIN aktivitetslogg_kontekst k ON k.aktivitetslogg_id = a.id
                WHERE k.kontekst_type = :kontekstType AND k.kontekst_verdi = :kontekstVerdi
                ORDER BY a.id ASC
                """.trimIndent(),
            paramMap = mapOf("kontekstType" to kontekstType, "kontekstVerdi" to kontekstVerdi),
        ),
    )

    override fun hentAktivitetslogg(ident: String) =
        hentAktivitetslogg(
            queryOf(
                //language=PostgreSQL
                statement = """SELECT * FROM aktivitetslogg""",
            ),
        )

    private fun hentAktivitetslogg(query: Query) =
        using(sessionOf(ds)) { session ->
            session.run(query.map { aktivitetsloggDTO(it.string("json")) }.asList)
        }

    private fun aktivitetsloggDTO(json: String) = jacksonObjectMapper.readValue<AktivitetsloggDTO>(json)

    @OptIn(DelicateCoroutinesApi::class)
    override fun lagre(
        uuid: UUID,
        ident: String,
        json: String,
    ) = using(sessionOf(ds)) { session ->
        session.transaction { tx ->
            val id =
                tx.run(
                    queryOf(
                        //language=PostgreSQL
                        statement =
                            """
                            INSERT INTO aktivitetslogg (melding_id, ident, json)
                            VALUES (:uuid, :ident, :json::jsonb)
                            RETURNING id
                            """.trimIndent(),
                        paramMap =
                            mapOf(
                                "uuid" to uuid,
                                "ident" to ident,
                                "json" to json,
                            ),
                    ).map { it.long("id") }.asSingle,
                )!!

            val kontekster = finnKontekster(json)
            for ((type, verdi) in kontekster) {
                tx.run(
                    queryOf(
                        //language=PostgreSQL
                        statement =
                            """
                            INSERT INTO aktivitetslogg_kontekst (aktivitetslogg_id, kontekst_type, kontekst_verdi)
                            VALUES (:id, :type, :verdi)
                            """.trimIndent(),
                        paramMap = mapOf("id" to id, "type" to type, "verdi" to verdi),
                    ).asUpdate,
                )
            }
            1
        }
    }.also {
        logger.info { "Annonserer ny aktivitetslogg med id=$uuid" }
        GlobalScope.launch {
            val emitted = messageSharedFlow.emit(listOf(aktivitetsloggDTO(json)))
            logger.info { "tryEmit as $emitted" }
        }
    }

    override fun flow() = messageSharedFlow.asSharedFlow()

    override fun hentTjenester(): List<TjenesteDTO> =
        using(sessionOf(ds)) { session ->
            session.run(
                queryOf(
                    //language=PostgreSQL
                    statement =
                        """
                        select distinct 
                            jsonb_array_elements(json->'system_participating_services')->>'service' as name
                        from aktivitetslogg
                        """.trimIndent(),
                ).map { row -> TjenesteDTO(name = row.string("name")) }.asList,
            )
        }

    override fun antallAktiviteter(): AntallAktiviteterDTO? =
        using(sessionOf(ds)) { session ->
            session.run(
                queryOf(
                    //language=PostgreSQL
                    statement =
                        """
                        select count(*)
                        from aktivitetslogg
                        """.trimIndent(),
                ).map { row -> AntallAktiviteterDTO(row.long("count")) }.asSingle,
            )
        }

    private fun finnKontekster(json: String): Set<Pair<String, String>> =
        try {
            val node = jacksonObjectMapper.readTree(json)
            node["aktiviteter"]
                ?.asSequence()
                ?.flatMap { aktivitet -> aktivitet["kontekster"]?.asSequence() ?: emptySequence() }
                ?.flatMap { kontekst ->
                    val type = kontekst["kontekstType"]?.asText() ?: return@flatMap emptySequence()
                    val map = kontekst["kontekstMap"] ?: return@flatMap emptySequence()
                    map.fields().asSequence().map { (key, value) -> "$type.$key" to value.asText() }
                }?.toSet() ?: emptySet()
        } catch (e: Exception) {
            logger.warn(e) { "Kunne ikke trekke ut kontekster fra aktivitetslogg" }
            emptySet()
        }
}
