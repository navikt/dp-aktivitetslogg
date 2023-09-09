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
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import no.nav.dagpenger.aktivitetslogg.aktivitetslogg.PostgresAktivitetsloggRepository
import no.nav.dagpenger.aktivitetslogg.api.models.AktivitetsloggDTO
import no.nav.dagpenger.aktivitetslogg.api.models.AntallAktiviteterDTO
import no.nav.dagpenger.aktivitetslogg.api.models.TjenesteDTO
import no.nav.dagpenger.aktivitetslogg.helpers.db.Postgres.withMigratedDb
import no.nav.dagpenger.aktivitetslogg.helpers.mockAzure
import org.postgresql.util.PSQLException
import java.util.UUID
import kotlin.test.Test

class AktivitetsloggApiTest {
    private val testToken by mockAzure

    private val første = UUID.randomUUID()
    private val andre = UUID.randomUUID()
    private val tredje = UUID.randomUUID()
    private val fjerde = UUID.randomUUID()
    private val aktivitetsloggRepository = PostgresAktivitetsloggRepository(withMigratedDb()).apply {
        lagre(første, "1", getData(første, "1"))
        lagre(andre, "2", getData(andre, "2"))
        lagre(tredje, "3", getData(tredje, "3"))
        lagre(fjerde, "3", getData(fjerde, "3"))
    }

    @Test
    fun `repository feiler på duplikater`() {
        shouldThrow<PSQLException> { aktivitetsloggRepository.lagre(fjerde, "1", getData(fjerde, "1")) }
    }

    @Test
    fun `kan hente med since og limit`() = testApplication {
        application { aktivitetsloggApi(aktivitetsloggRepository) }

        client().get("/aktivitetslogg?since=$andre&limit=2") {
            header(HttpHeaders.Authorization, "Bearer $testToken")
        }.apply {
            status shouldBe HttpStatusCode.OK
            val response = this.body<List<AktivitetsloggDTO>>()
            response.size shouldBe 2

            response[0].atId shouldBe fjerde.toString()
            response[1].atId shouldBe tredje.toString()
        }
    }

    @Test
    fun `kan hente på ident`() = testApplication {
        application { aktivitetsloggApi(aktivitetsloggRepository) }

        client().get("/aktivitetslogg?ident=3") {
            header(HttpHeaders.Authorization, "Bearer $testToken")
        }.apply {
            status shouldBe HttpStatusCode.OK
            val response = this.body<List<AktivitetsloggDTO>>()

            response.size shouldBe 2

            response[0].ident shouldBe "3"
            response[1].ident shouldBe "3"
        }

        client().get("/aktivitetslogg?ident=333") {
            header(HttpHeaders.Authorization, "Bearer $testToken")
        }.apply {
            status shouldBe HttpStatusCode.OK
            val response = this.body<List<AktivitetsloggDTO>>()

            response.size shouldBe 0
        }
    }

    @Test
    fun `kan hente på tjeneste`() = testApplication {
        application { aktivitetsloggApi(aktivitetsloggRepository) }

        client().get("/aktivitetslogg?tjeneste=dp-vedtak") {
            header(HttpHeaders.Authorization, "Bearer $testToken")
        }.apply {
            status shouldBe HttpStatusCode.OK
            val response = this.body<List<AktivitetsloggDTO>>()

            response.size shouldBe 0
        }

        client().get("/aktivitetslogg?tjeneste=dp-rapportering") {
            header(HttpHeaders.Authorization, "Bearer $testToken")
        }.apply {
            status shouldBe HttpStatusCode.OK
            val response = this.body<List<AktivitetsloggDTO>>()

            response.size shouldBe 4

            response[0].systemParticipatingServices[0].service shouldBe "dp-rapportering"
        }
    }

    @Test
    fun `kan hente uten argument`() = testApplication {
        application { aktivitetsloggApi(aktivitetsloggRepository) }

        client().get("/aktivitetslogg") {
            header(HttpHeaders.Authorization, "Bearer $testToken")
        }.apply {
            status shouldBe HttpStatusCode.OK
            val response = this.body<List<AktivitetsloggDTO>>()
            response.size shouldBe 4

            response[0].atId shouldBe fjerde.toString()
            response[1].atId shouldBe tredje.toString()
            response[2].atId shouldBe andre.toString()
            response[3].atId shouldBe første.toString()
        }
    }

    @Test
    fun `kan vente på nye meldinger`() = testApplication {
        application { aktivitetsloggApi(aktivitetsloggRepository) }

        val nyAktivitetslogg = UUID.randomUUID()
        runBlocking {
            async {
                client().get("/aktivitetslogg?since=$fjerde&wait=true") {
                    header(HttpHeaders.Authorization, "Bearer $testToken")
                }.apply {
                    status shouldBe HttpStatusCode.OK
                    val response = this.body<List<AktivitetsloggDTO>>()
                    response.size shouldBe 1

                    response[0].atId shouldBe nyAktivitetslogg.toString()
                }
            }
            async {
                aktivitetsloggRepository.lagre(nyAktivitetslogg, "1", getData(nyAktivitetslogg, "1"))
            }
        }
    }

    @Test
    fun `kan hente alle tjenester`() = testApplication {
        application { aktivitetsloggApi(aktivitetsloggRepository) }

        client().get("/aktivitetslogg/tjenester") {
            header(HttpHeaders.Authorization, "Bearer $testToken")
        }.apply {
            status shouldBe HttpStatusCode.OK
            val response = this.body<List<TjenesteDTO>>()
            response.size shouldBe 2

        }
    }

    @Test
    fun `kan hente antall aktiviteter`() = testApplication {
        application { aktivitetsloggApi(aktivitetsloggRepository) }

        client().get("/aktivitetslogg/antall") {
            header(HttpHeaders.Authorization, "Bearer $testToken")
        }.apply {
            status shouldBe HttpStatusCode.OK
            val antallAktiviteter = this.body<AntallAktiviteterDTO>()
            antallAktiviteter.antall shouldBe 4
        }
    }

    private fun ApplicationTestBuilder.client() = createClient {
        install(ContentNegotiation) {
            jackson { registerModule(JavaTimeModule()) }
        }
    }
}

fun getData(atId: UUID, ident: String) = """
{
  "@id": "$atId",
  "ident": "$ident",
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
            "ident": "$ident",
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
