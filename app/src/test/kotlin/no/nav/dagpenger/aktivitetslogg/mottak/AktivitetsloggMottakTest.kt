package no.nav.dagpenger.aktivitetslogg.mottak

import io.kotest.matchers.shouldBe
import no.nav.dagpenger.aktivitetslogg.aktivitetslogg.PostgresAktivitetsloggRepository
import no.nav.dagpenger.aktivitetslogg.db.Postgres.withMigratedDb
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
        rapid.sendTestMessage(
            //language=JSON
            """{
            |  "@event_name": "aktivitetslogg",
            |  "ident": "ident"
            |}
            """.trimMargin(),
        )

        with(repository.hentAktivitetslogg("ident").first()) {
            this.atEventName shouldBe "aktivitetslogg"
            this.ident shouldBe "ident"
        }
    }
}
