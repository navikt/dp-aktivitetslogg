package no.nav.dagpenger.aktivitetslogg

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class AuditKontekstTest {

    @Test
    fun `Audit kontekst er også en aktivitetskontekst`() {
        val auditKontekst = AuditKontekst(
            borgerIdent = "audit",
            saksbehandlerNavIdent = "diam",
            alvorlighetsgrad = AuditKontekst.Alvorlighetsgrad.INFO,
            operasjon = AuditKontekst.Operasjon.READ
        )

        val spesifikkKontekst = auditKontekst.toSpesifikkKontekst()

        spesifikkKontekst.kontekstType shouldBe "audit"
        spesifikkKontekst.kontekstMap shouldBe mapOf(
            "appName" to "dagpenger-aktivitetslogg-ukjent",
            "borgerIdent" to "audit",
            "saksbehandlerNavIdent" to "diam",
            "alvorlighetsgrad" to "INFO",
            "operasjon" to "READ"
        )
    }
}