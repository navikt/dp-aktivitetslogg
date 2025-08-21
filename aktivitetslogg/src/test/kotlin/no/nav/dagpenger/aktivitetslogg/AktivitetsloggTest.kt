package no.nav.dagpenger.aktivitetslogg

import io.kotest.matchers.shouldBe
import no.nav.dagpenger.aktivitetslogg.aktivitet.Behov
import no.nav.dagpenger.aktivitetslogg.aktivitet.Info
import no.nav.dagpenger.aktivitetslogg.aktivitet.LogiskFeil
import no.nav.dagpenger.aktivitetslogg.aktivitet.Varsel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.util.UUID

internal class AktivitetsloggTest {
    private lateinit var aktivitetslogg: Aktivitetslogg
    private lateinit var testKontekst: TestKontekst

    @BeforeEach
    fun setUp() {
        aktivitetslogg = Aktivitetslogg()
        testKontekst = TestKontekst("Person")
    }

    @Test
    fun `info should add an Aktivitet Info to the log`() {
        aktivitetslogg.info("This is an info message")
        assertEquals(1, aktivitetslogg.aktivitetsteller())
        assertTrue(aktivitetslogg.behov().isEmpty())
    }

    @Test
    fun `inneholder original melding`() {
        val infomelding = "info message"
        aktivitetslogg.info(infomelding)
        assertInfo(infomelding)
    }

    @Test
    fun `logisk feil oppdaget og kaster exception`() {
        val melding = "Severe error"
        assertThrows<Aktivitetslogg.AktivitetException> { aktivitetslogg.logiskFeil(melding) }
        // assertTrue(aktivitetslogg.hasErrors())
        assertTrue(aktivitetslogg.toString().contains(melding))
        assertLogiskfeil(melding)
    }

    @Test
    fun `Melding sendt til forelder`() {
        val hendelse =
            TestHendelse(
                aktivitetslogg.barn(),
            )
        "info message".also {
            hendelse.info(it)
            assertInfo(it, hendelse.logg)
            assertInfo(it, aktivitetslogg)
        }
    }

    @Test
    fun `Melding sendt fra barnebarn til forelder`() {
        val hendelse =
            TestHendelse(
                aktivitetslogg.barn(),
            )
        hendelse.kontekst(testKontekst)
        val testKontekst2 =
            TestKontekst("Melding")
        hendelse.kontekst(testKontekst2)
        val testKontekst3 =
            TestKontekst("Soknad")
        hendelse.kontekst(testKontekst3)
        "info message".also {
            hendelse.info(it)
            assertInfo(it, hendelse.logg)
            assertInfo(it, aktivitetslogg)
        }
    }

    @Test
    fun `Vis bare arbeidsgiveraktivitet`() {
        val hendelse1 =
            TestHendelse(
                aktivitetslogg.barn(),
            )
        hendelse1.kontekst(testKontekst)
        val arbeidsgiver1 =
            TestKontekst("Arbeidsgiver 1")
        hendelse1.kontekst(arbeidsgiver1)
        val vedtaksperiode1 =
            TestKontekst("Vedtaksperiode 1")
        hendelse1.kontekst(vedtaksperiode1)
        hendelse1.info("info message")
        hendelse1.info("annen info message")
        val hendelse2 =
            TestHendelse(
                aktivitetslogg.barn(),
            )
        hendelse2.kontekst(testKontekst)
        val arbeidsgiver2 =
            TestKontekst("Arbeidsgiver 2")
        hendelse2.kontekst(arbeidsgiver2)
        val vedtaksperiode2 =
            TestKontekst("Vedtaksperiode 2")
        hendelse2.kontekst(vedtaksperiode2)
        hendelse2.info("info message")
        assertEquals(3, aktivitetslogg.aktivitetsteller())
        assertEquals(2, aktivitetslogg.logg(vedtaksperiode1).aktivitetsteller())
        assertEquals(1, aktivitetslogg.logg(arbeidsgiver2).aktivitetsteller())
    }

    @Test
    fun `Behov kan ha detaljer`() {
        val hendelse1 =
            TestHendelse(
                aktivitetslogg.barn(),
            )
        hendelse1.kontekst(testKontekst)
        val param1 = "value"
        val param2 = LocalDate.now()
        hendelse1.behov(
            type = TestBehov.Test,
            melding = "Behov om test",
            detaljer =
                mapOf(
                    "param1" to param1,
                    "param2" to param2,
                ),
        )

        assertEquals(1, aktivitetslogg.behov().size)
        assertEquals(
            1,
            aktivitetslogg
                .behov()
                .first()
                .kontekst()
                .size,
        )
        assertEquals(
            2,
            aktivitetslogg
                .behov()
                .first()
                .detaljer()
                .size,
        )
        assertEquals("Person", aktivitetslogg.behov().first().kontekst()["Person"])
        assertEquals(param1, aktivitetslogg.behov().first().detaljer()["param1"])
        assertEquals(param2, aktivitetslogg.behov().first().detaljer()["param2"])
    }

    @Test
    fun `det kan legges på AuditKontekst`() {
        val hendelse = TestHendelse(aktivitetslogg)

        // API / mediator
        hendelse.info("Heihei", "12345678901", "X123456", AuditOperasjon.READ)
        assertInfo("Heihei")

        hendelse.varsel("Neinei", "12345678901", "X123456", AuditOperasjon.READ)
        assertVarsel("Neinei")

        // I modellen
        hendelse.info("Dette skjedde i modellen")
    }

    @Test
    fun `varsel er en aktivitet`() {
        val hendelse = TestHendelse(aktivitetslogg)
        hendelse.varsel("Neinei", "12345678901", "X123456", AuditOperasjon.READ)
        assertVarsel("Neinei")
        aktivitetslogg.harAktiviteter() shouldBe true
    }

    private fun assertLogiskfeil(
        message: String,
        aktivitetslogg: Aktivitetslogg = this.aktivitetslogg,
    ) {
        var visitorCalled = false
        aktivitetslogg.accept(
            object : AktivitetsloggVisitor {
                override fun visitWarn(
                    id: UUID,
                    kontekster: List<SpesifikkKontekst>,
                    aktivitet: LogiskFeil,
                    melding: String,
                    tidsstempel: String,
                ) {
                    visitorCalled = true
                    assertEquals(message, melding)
                }
            },
        )
        assertTrue(visitorCalled)
    }

    private fun assertInfo(
        message: String,
        aktivitetslogg: Aktivitetslogg = this.aktivitetslogg,
    ) {
        var visitorCalled = false
        aktivitetslogg.accept(
            object : AktivitetsloggVisitor {
                override fun visitInfo(
                    id: UUID,
                    kontekster: List<SpesifikkKontekst>,
                    aktivitet: Info,
                    melding: String,
                    tidsstempel: String,
                ) {
                    visitorCalled = true
                    assertEquals(message, melding)
                }
            },
        )
        assertTrue(visitorCalled)
    }

    private fun assertVarsel(
        message: String,
        aktivitetslogg: Aktivitetslogg = this.aktivitetslogg,
    ) {
        var visitorCalled = false
        aktivitetslogg.accept(
            object : AktivitetsloggVisitor {
                override fun visitVarsel(
                    id: UUID,
                    kontekster: List<SpesifikkKontekst>,
                    varsel: Varsel,
                    kode: Varselkode?,
                    melding: String,
                    tidsstempel: String,
                ) {
                    visitorCalled = true
                    assertEquals(message, melding)
                }
            },
        )
        assertTrue(visitorCalled)
    }

    private enum class TestBehov : Behov.Behovtype {
        Test,
    }

    private class TestKontekst(
        private val melding: String,
    ) : Aktivitetskontekst {
        override fun toSpesifikkKontekst() = SpesifikkKontekst(melding, mapOf(melding to melding))
    }

    private class TestHendelse(
        val logg: Aktivitetslogg,
    ) : Aktivitetskontekst,
        IAktivitetslogg by logg {
        init {
            logg.kontekst(this)
        }

        override fun toSpesifikkKontekst() = SpesifikkKontekst("TestHendelse")

        override fun kontekst(kontekst: Aktivitetskontekst) {
            logg.kontekst(kontekst)
        }
    }
}
