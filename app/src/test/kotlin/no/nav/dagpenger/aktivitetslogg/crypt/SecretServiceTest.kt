package no.nav.dagpenger.aktivitetslogg.crypt

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test

class SecretServiceTest {
    private val ident = "12345678987"
    private val secretService = SecretService()

    @Test
    fun `encrypted ident skal ikke være lik ident i klar tekst`() {
        val encryptedIdent = secretService.encrypt(ident, secretService.publicKeyAsString())

        encryptedIdent shouldNotBe ident
    }

    @Test
    fun `decrypted ident skal være lik ident i klar tekst`() {
        val decryptedIdent = secretService.decrypt(secretService.encrypt(ident, secretService.publicKeyAsString()))

        decryptedIdent shouldBe ident
    }
}