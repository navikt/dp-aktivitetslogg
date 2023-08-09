package no.nav.dagpenger.aktivitetslogg.api

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.jackson
import io.ktor.server.testing.testApplication
import no.nav.dagpenger.aktivitetslogg.aktivitetslogg.PostgresAktivitetsloggRepository
import no.nav.dagpenger.aktivitetslogg.api.models.AktivitetsloggDTO
import no.nav.dagpenger.aktivitetslogg.helpers.db.Postgres.withMigratedDb
import no.nav.dagpenger.aktivitetslogg.helpers.mockAzure
import org.postgresql.util.PSQLException
import java.util.UUID
import kotlin.test.Test

class AktivitetsloggApiTest {
    private val testToken by mockAzure

    @Test
    fun testGetAktivitetslogg() = testApplication {
        val aktivitetsloggRepository = PostgresAktivitetsloggRepository(withMigratedDb()).apply {
            lagre(UUID.randomUUID(), "123", data)
            lagre(UUID.randomUUID(), "123", data)
            lagre(UUID.randomUUID(), "123", data)
            with(UUID.randomUUID()) {
                lagre(this, "123", data)
                shouldThrow<PSQLException> { lagre(this, "123", data) }
            }
        }

        application {
            aktivitetsloggApi(aktivitetsloggRepository)
        }

        client.config {
            install(ContentNegotiation) {
                jackson {
                    registerModule(JavaTimeModule())
                }
            }
        }.get("/aktivitetslogg?offset=1&limit=2") {
            header(HttpHeaders.Authorization, "Bearer $testToken")
        }.apply {
            status shouldBe HttpStatusCode.OK
            val response = this.body<List<AktivitetsloggDTO>>()
            response.size shouldBe 2
        }
    }
}

val data = """
{
  "@id": "d70ece5f-6706-4e78-a23d-0218a23559c5",
  "ident": "29838099503",
  "hendelse": {
    "type": "BeregningsdatoPassertHendelse",
    "meldingsreferanseId": "fdeb70cd-da1f-4f08-b508-58352811fcd5"
  },
  "@opprettet": "2023-08-09T10:07:32.215009157",
  "@event_name": "aktivitetslogg",
  "aktiviteter": [
    {
      "melding": "Rapporteringsperioden skal ikke beregnes på grunn av strategi",
      "detaljer": {},
      "kontekster": [
        {
          "kontekstMap": {
            "ident": "29838099503",
            "meldingsreferanseId": "fdeb70cd-da1f-4f08-b508-58352811fcd5"
          },
          "kontekstType": "BeregningsdatoPassertHendelse"
        },
        {
          "kontekstMap": {
            "fom": "2023-07-10",
            "tom": "2023-07-23"
          },
          "kontekstType": "Rapporteringsperiode"
        },
        {
          "kontekstMap": {
            "tilstand": "Godkjent"
          },
          "kontekstType": "Tilstand"
        }
      ],
      "tidsstempel": "2023-08-09 10:07:32.119",
      "alvorlighetsgrad": "INFO"
    }
  ],
  "system_read_count": 1,
  "system_participating_services": [
    {
      "id": "d70ece5f-6706-4e78-a23d-0218a23559c5",
      "time": "2023-08-09T10:07:32.215009157",
      "image": "europe-north1-docker.pkg.dev/nais-management-233d/teamdagpenger/dp-rapportering:2023.08.09-07.35-242c3a6",
      "service": "dp-rapportering",
      "instance": "dp-rapportering-8ccf6d78d-h4rrj"
    },
    {
      "id": "d70ece5f-6706-4e78-a23d-0218a23559c5",
      "time": "2023-08-09T08:07:32.221241183",
      "image": "europe-north1-docker.pkg.dev/nais-management-233d/teamdagpenger/dp-aktivitetslogg:2023.08.09-07.49-8d442ef",
      "service": "dp-aktivitetslogg",
      "instance": "dp-aktivitetslogg-654ddd875b-psps7"
    }
  ]
} 
""".trimIndent()
