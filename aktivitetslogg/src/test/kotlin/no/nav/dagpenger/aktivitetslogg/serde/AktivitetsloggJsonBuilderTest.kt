package no.nav.dagpenger.aktivitetslogg.serde

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.assertions.json.FieldComparison
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.assertions.throwables.shouldThrow
import no.nav.dagpenger.aktivitetslogg.Aktivitetslogg
import no.nav.dagpenger.aktivitetslogg.AuditOperasjon
import no.nav.dagpenger.aktivitetslogg.aktivitet.Behov
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test

internal class AktivitetsloggJsonBuilderTest {
    private lateinit var aktivitetslogg: Aktivitetslogg

    @BeforeEach
    fun setUp() {
        aktivitetslogg = Aktivitetslogg()
    }

    @Test
    fun `json representasjon av aktitvitetslogg`() {
        aktivitetslogg.info("infomelding")
        aktivitetslogg.info("auditmelding", "ceteros", "tation", AuditOperasjon.CREATE)
        aktivitetslogg.behov(
            TestBehov,
            "behovmelding",
            mapOf(
                "detalj" to "detaljert",
            ),
        )
        aktivitetslogg.varsel("varselmelding")
        shouldThrow<Aktivitetslogg.AktivitetException> { aktivitetslogg.logiskFeil("logisk feil") }

        aktivitetslogg.asJson().shouldEqualJson {
            fieldComparison = FieldComparison.Lenient
            expectedAktivitetsloggJson()
        }
    }

    //language=JSON
    private fun expectedAktivitetsloggJson() =
        """[
  {
    "kontekster": [],
    "alvorlighetsgrad": "INFO",
    "melding": "infomelding",
    "detaljer": {}
  },
  {
    "kontekster": [
      {
        "kontekstType": "audit",
        "kontekstMap": {
          "appName": "dagpenger-aktivitetslogg-ukjent",
          "borgerIdent": "ceteros",
          "saksbehandlerNavIdent": "tation",
          "alvorlighetsgrad": "INFO",
          "operasjon": "CREATE"
        }
      }
    ],
    "alvorlighetsgrad": "INFO",
    "melding": "auditmelding",
    "detaljer": {}
  },
  {
    "kontekster": [
    ],
    "alvorlighetsgrad": "BEHOV",
    "behovtype": "TEST_BEHOV",
    "melding": "behovmelding",
    "detaljer": {
      "detalj": "detaljert"
    }
  },
  {
    "kontekster": [
    ],
    "alvorlighetsgrad": "ERROR",
    "melding": "varselmelding",
    "detaljer": {}
  },
  {
    "kontekster": [
    ],
    "alvorlighetsgrad": "WARN",
    "melding": "logisk feil",
    "detaljer": {}
  }
]
"""

    object TestBehov : Behov.Behovtype {
        override val name: String
            get() = "TEST_BEHOV"
    }

    private fun Aktivitetslogg.asJson() = jsonMapper.writeValueAsString(AktivitetsloggJsonBuilder(this).asList())

    private companion object {
        val jsonMapper = jacksonObjectMapper()
    }
}
