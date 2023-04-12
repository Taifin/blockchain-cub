import javax.crypto.Cipher
import kotlinx.cli.*
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.io.FileReader
import java.io.FileWriter
import java.lang.IllegalArgumentException
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.xml.bind.DatatypeConverter

fun generateKeyPair(): KeyPair {
    val keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC")
    keyPairGenerator.initialize(512)
    return keyPairGenerator.generateKeyPair()
}

fun encrypt(text: String, key: PublicKey): ByteArray {
    val cipher = Cipher.getInstance("RSA", "BC")
    cipher.init(Cipher.ENCRYPT_MODE, key)
    return cipher.doFinal(text.toByteArray(Charsets.UTF_8))
}

fun decrypt(encrypted: ByteArray, key: PrivateKey): String {
    val cipher = Cipher.getInstance("RSA", "BC")
    cipher.init(Cipher.DECRYPT_MODE, key)
    return String(cipher.doFinal(encrypted), Charsets.UTF_8)
}

fun extractPublicKey(keyData: ByteArray): PublicKey {
    val keyFactory = KeyFactory.getInstance("RSA", "BC")
    return keyFactory.generatePublic(X509EncodedKeySpec(keyData))
}

fun extractPrivateKey(keyData: ByteArray): PrivateKey {
    val keyFactory = KeyFactory.getInstance("RSA", "BC")
    return keyFactory.generatePrivate(PKCS8EncodedKeySpec(keyData))
}

enum class Commands {
    ENCRYPT,
    DECRYPT,
    GENERATE
}

fun main(args: Array<String>) {
    val parser = ArgParser("taifin-bc-ecdsa")
    val inputFile by parser.option(ArgType.String, shortName = "i", description = "Input file")
    val outputFile by parser.option(ArgType.String, shortName = "o", description = "Output file")
    val keyFile by parser.option(
        ArgType.String,
        shortName = "k",
        description = "Public or private key file"
    )
    val command by parser.argument(ArgType.Choice<Commands>(), description = "Command")
    parser.parse(args)
    Security.addProvider(BouncyCastleProvider())

    val key: String?
    if (command != Commands.GENERATE && keyFile == null) throw IllegalArgumentException()
    else {
        key = keyFile?.let { FileReader(it).readText() }
    }

    when (command) {
        Commands.DECRYPT -> {
            if (inputFile == null || outputFile == null) throw IllegalArgumentException()
            val data = inputFile?.let { FileReader(it).readText() }
            FileWriter(outputFile!!).use {
                it.write(
                    decrypt(
                        DatatypeConverter.parseHexBinary(data!!),
                        extractPrivateKey(DatatypeConverter.parseHexBinary(key))
                    )
                )
            }
        }

        Commands.ENCRYPT -> {
            if (inputFile == null || outputFile == null) throw IllegalArgumentException()
            val data = inputFile?.let { FileReader(it).readText() }
            FileWriter(outputFile!!).use {
                it.write(
                    DatatypeConverter.printHexBinary(
                        encrypt(
                            data!!,
                            extractPublicKey(DatatypeConverter.parseHexBinary(key))
                        )
                    )
                )
            }
        }

        Commands.GENERATE -> {
            val keys = generateKeyPair()
            println("Public key generated: ${keys.public}")
            println("Private key generated: ${keys.private}")
            println("Keys are saved to \"public_key.key\" and \"private_key.key\" files respectively.")
            FileWriter("public_key.key").use { writer ->
                writer.write(
                    DatatypeConverter.printHexBinary(
                        keys.public.encoded
                    )
                )
            }
            FileWriter("private_key.key").use { writer ->
                writer.write(
                    DatatypeConverter.printHexBinary(
                        keys.private.encoded
                    )
                )
            }
        }
    }
}
