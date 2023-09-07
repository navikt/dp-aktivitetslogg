package no.nav.dagpenger.aktivitetslogg.mottak

import io.kotest.matchers.shouldBe
import no.nav.dagpenger.aktivitetslogg.aktivitetslogg.PostgresAktivitetsloggRepository
import no.nav.dagpenger.aktivitetslogg.api.models.AktivitetDTO
import no.nav.dagpenger.aktivitetslogg.helpers.db.Postgres.withMigratedDb
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.util.UUID

class AktivitetsloggMottakTest {
    private val repository by lazy { PostgresAktivitetsloggRepository(withMigratedDb()) }
    private val rapid by lazy {
        TestRapid().apply {
            AktivitetsloggMottak(this, repository)
        }
    }

    @AfterEach
    fun tearDown() {
        rapid.reset()
    }

    @Test
    fun `lagrer meldinger med aktivitetslogg`() {
        val newMessage =
            JsonMessage.newMessage(
                "aktivitetslogg",
                mapOf(
                    "ident" to "ident",
                    "hendelse" to mapOf("type" to "bar", "meldingsreferanseId" to UUID.randomUUID()),
                    "aktiviteter" to emptyList<Any>(),
                ),
            )
        rapid.sendTestMessage(newMessage.toJson())

        with(repository.hentAktivitetslogg("ident").first()) {
            this.atEventName shouldBe "aktivitetslogg"
            this.ident shouldBe "ident"
        }
    }
}
