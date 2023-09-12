package no.nav.dagpenger.aktivitetslogg.crypt

import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import javax.crypto.Cipher


fun SecretService.encrypt(ident: String, publicKeyString: String): String {

    val publicKey = KeyFactory
        .getInstance("RSA")
        .generatePublic(
            X509EncodedKeySpec(
                Base64
                    .getDecoder()
                    .decode(publicKeyString)
            )
        )

    val encryptCipher = Cipher.getInstance("RSA")
    encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey)

    return Base64
        .getEncoder()
        .encodeToString(encryptCipher.doFinal(ident.toByteArray(StandardCharsets.UTF_8)))
}