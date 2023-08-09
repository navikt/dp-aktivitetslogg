package no.nav.dagpenger.aktivitetslogg.api

import io.kotest.matchers.shouldBe
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import no.nav.dagpenger.aktivitetslogg.aktivitetslogg.PostgresAktivitetsloggRepository
import no.nav.dagpenger.aktivitetslogg.db.Postgres.withMigratedDb
import no.nav.dagpenger.aktivitetslogg.helpers.mockAzure
import kotlin.test.Test

class AktivitetsloggApiTest {
    private val testToken by mockAzure

    @Test
    fun testGetAktivitetslogg() = testApplication {
        application {
            aktivitetsloggApi(aktivitetsloggRepository = PostgresAktivitetsloggRepository(withMigratedDb()))
        }
        client.get("/aktivitetslogg") {
            header(HttpHeaders.Authorization, "Bearer $testToken")
        }.apply {
            status shouldBe HttpStatusCode.OK
            this.bodyAsText() shouldBe "[]"
        }
    }
}
