package hashcli.hash

import java.io.File
import java.io.InputStream
import java.security.MessageDigest
import java.util.Locale
import java.util.zip.CRC32

enum class HashAlgorithm(
    val cliValue: String,
    val displayName: String,
    private val messageDigestName: String? = null,
) {
    MD5("md5", "MD5", "MD5"),
    SHA1("sha1", "SHA1", "SHA-1"),
    SHA256("sha256", "SHA256", "SHA-256"),
    CRC32("crc32", "CRC32");

    companion object {
        fun parse(value: String): HashAlgorithm? {
            val normalized = value.lowercase(Locale.ROOT)
            return entries.firstOrNull { it.cliValue == normalized }
        }

        fun defaultSelection(): List<HashAlgorithm> = listOf(MD5, SHA1, SHA256, CRC32)
    }

    internal fun newMessageDigest(): MessageDigest? = messageDigestName?.let(MessageDigest::getInstance)
}

data class HashValue(
    val algorithm: HashAlgorithm,
    val value: String,
)

object FileHashUtils {
    fun calculate(
        path: File,
        algorithms: Collection<HashAlgorithm> = HashAlgorithm.defaultSelection(),
        uppercase: Boolean = false,
    ): List<HashValue> {
        return path.inputStream().use { input ->
            calculate(input, algorithms, uppercase)
        }
    }

    fun calculate(
        input: InputStream,
        algorithms: Collection<HashAlgorithm> = HashAlgorithm.defaultSelection(),
        uppercase: Boolean = false,
    ): List<HashValue> {
        val selected = algorithms.distinct()
        require(selected.isNotEmpty()) { "At least one hash algorithm must be selected." }

        val digests = selected
            .mapNotNull { algorithm -> algorithm.newMessageDigest()?.let { algorithm to it } }
            .toMap()
        val crc32 = if (HashAlgorithm.CRC32 in selected) CRC32() else null
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)

        while (true) {
            val read = input.read(buffer)
            if (read < 0) {
                break
            }
            digests.values.forEach { it.update(buffer, 0, read) }
            crc32?.update(buffer, 0, read)
        }

        return selected.map { algorithm ->
            HashValue(
                algorithm = algorithm,
                value = when (algorithm) {
                    HashAlgorithm.CRC32 -> crc32
                        ?.value
                        ?.toUInt()
                        ?.toString(16)
                        ?.padStart(8, '0')
                        ?.formatCase(uppercase)
                        ?: error("CRC32 digest was not initialized")

                    else -> digests.getValue(algorithm).digest().toHex(uppercase)
                },
            )
        }
    }
}

private fun ByteArray.toHex(uppercase: Boolean): String {
    val text = joinToString(separator = "") { byte -> "%02x".format(byte.toInt() and 0xff) }
    return text.formatCase(uppercase)
}

private fun String.formatCase(uppercase: Boolean): String {
    return if (uppercase) uppercase(Locale.ROOT) else lowercase(Locale.ROOT)
}
