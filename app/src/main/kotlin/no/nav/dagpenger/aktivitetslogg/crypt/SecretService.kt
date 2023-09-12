package no.nav.dagpenger.aktivitetslogg.crypt

import java.nio.charset.StandardCharsets
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher

class SecretService {

    private val keyPair: KeyPair

    init {
        val instance = KeyPairGenerator.getInstance("RSA")
        instance.initialize(2048, SecureRandom())
        this.keyPair = instance.generateKeyPair()
    }

    fun publicKeyAsString(): String = Base64.getEncoder().encodeToString(keyPair.public.encoded)

    fun decrypt(encryptetIden: String): String {
        val decryptCipher = Cipher.getInstance("RSA")
        decryptCipher.init(Cipher.DECRYPT_MODE, keyPair.private)

        return String(
            decryptCipher.doFinal(
                Base64.getDecoder().decode(encryptetIden.toByteArray(StandardCharsets.UTF_8))
            )
        )
    }

}