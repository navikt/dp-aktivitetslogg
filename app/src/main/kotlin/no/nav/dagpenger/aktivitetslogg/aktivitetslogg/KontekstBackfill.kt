package no.nav.dagpenger.aktivitetslogg.aktivitetslogg

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.delay
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import javax.sql.DataSource

private val logger = KotlinLogging.logger {}

internal class KontekstBackfill(
    private val ds: DataSource,
    private val batchSize: Int = 500,
    private val delayMs: Long = 100,
) {
    private val repository = PostgresAktivitetsloggRepository(ds)

    suspend fun run() {
        logger.info { "Starter kontekst-backfill" }
        var totalProcessed = 0
        var lastId = 0L

        while (true) {
            val batch = hentBatch(lastId)
            if (batch.isEmpty()) break

            for ((id, json) in batch) {
                val kontekster = finnKontekster(json)
                if (kontekster.isNotEmpty()) {
                    insertKontekster(id, kontekster)
                }
                lastId = id
                totalProcessed++
            }

            if (totalProcessed % 5000 == 0) {
                logger.info { "Kontekst-backfill: $totalProcessed rader prosessert (sist id=$lastId)" }
            }

            delay(delayMs)
        }

        logger.info { "Kontekst-backfill ferdig: $totalProcessed rader prosessert totalt" }
    }

    private fun hentBatch(afterId: Long): List<Pair<Long, String>> =
        using(sessionOf(ds)) { session ->
            session.run(
                queryOf(
                    //language=PostgreSQL
                    """
                    SELECT a.id, a.json
                    FROM aktivitetslogg a
                    LEFT JOIN aktivitetslogg_kontekst k ON k.aktivitetslogg_id = a.id
                    WHERE a.id > :afterId AND k.id IS NULL
                    ORDER BY a.id ASC
                    LIMIT :limit
                    """.trimIndent(),
                    mapOf("afterId" to afterId, "limit" to batchSize),
                ).map { row -> row.long("id") to row.string("json") }.asList,
            )
        }

    private fun insertKontekster(
        aktivitetsloggId: Long,
        kontekster: Set<Pair<String, String>>,
    ) {
        using(sessionOf(ds)) { session ->
            session.transaction { tx ->
                for ((type, verdi) in kontekster) {
                    tx.run(
                        queryOf(
                            //language=PostgreSQL
                            """
                            INSERT INTO aktivitetslogg_kontekst (aktivitetslogg_id, kontekst_type, kontekst_verdi)
                            VALUES (:id, :type, :verdi)
                            ON CONFLICT DO NOTHING
                            """.trimIndent(),
                            mapOf("id" to aktivitetsloggId, "type" to type, "verdi" to verdi),
                        ).asUpdate,
                    )
                }
            }
        }
    }

    private fun finnKontekster(json: String): Set<Pair<String, String>> =
        try {
            val node = no.nav.dagpenger.aktivitetslogg.serialisering.jacksonObjectMapper.readTree(json)
            node["aktiviteter"]
                ?.asSequence()
                ?.flatMap { aktivitet -> aktivitet["kontekster"]?.asSequence() ?: emptySequence() }
                ?.flatMap { kontekst ->
                    val type = kontekst["kontekstType"]?.asText() ?: return@flatMap emptySequence()
                    val map = kontekst["kontekstMap"] ?: return@flatMap emptySequence()
                    map.fields().asSequence().map { (key, value) -> "$type.$key" to value.asText() }
                }?.toSet() ?: emptySet()
        } catch (e: Exception) {
            logger.warn(e) { "Kunne ikke trekke ut kontekster under backfill" }
            emptySet()
        }
}
