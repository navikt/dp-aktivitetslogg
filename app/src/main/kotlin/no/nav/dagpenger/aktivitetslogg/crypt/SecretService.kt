package no.nav.dagpenger.aktivitetslogg.crypt

import mu.KotlinLogging
import java.nio.charset.StandardCharsets
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher

class SecretService {

    private val sikkerLogger = KotlinLogging.logger("tjenestkall")
    private val keyPair: KeyPair

    init {
        val instance = KeyPairGenerator.getInstance("RSA")
        instance.initialize(2048, SecureRandom())
        this.keyPair = instance.generateKeyPair()

        sikkerLogger.info { "PublicKey: ${keyPair.private}" }
    }

    fun publicKeyAsString(): String = Base64.getEncoder().encodeToString(keyPair.public.encoded)

    fun privateKey(): PrivateKey = keyPair.private

//    fun decrypt(encryptetIden: String): String {
//        val decryptCipher = Cipher.getInstance("RSA")
//        decryptCipher.init(Cipher.DECRYPT_MODE, keyPair.private)
//
//        return String(
//            decryptCipher.doFinal(
//                Base64.getDecoder().decode(encryptetIden.toByteArray(StandardCharsets.UTF_8))
//            )
//        )
//    }

}

fun String.toDecryptedStringOrNull(privateKey: PrivateKey): String? {
    return if (this == "null") {
        null
    } else {
        val decryptCipher = Cipher.getInstance("RSA")
        decryptCipher.init(Cipher.DECRYPT_MODE, privateKey)

        String(
            decryptCipher.doFinal(
                Base64.getDecoder().decode(this.toByteArray(StandardCharsets.UTF_8))
            )
        )
    }
}