package no.nav.dagpenger.aktivitetslogg.mottak

import io.kotest.matchers.shouldBe
import no.nav.dagpenger.aktivitetslogg.aktivitetslogg.PostgresAktivitetsloggRepository
import no.nav.dagpenger.aktivitetslogg.helpers.db.Postgres.withMigratedDb
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

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
        rapid.sendTestMessage(JsonMessage.newMessage("aktivitetslogg", mapOf("ident" to "ident")).toJson())

        with(repository.hentAktivitetslogg("ident").first()) {
            this.atEventName shouldBe "aktivitetslogg"
            this.ident shouldBe "ident"
        }
    }
}
