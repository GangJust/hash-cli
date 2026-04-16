package hashcli

import hashcli.hash.HashAlgorithm
import hashcli.hash.HashValue
import kotlin.test.Test
import kotlin.test.assertEquals

class VerificationFormatterTest {
    @Test
    fun `renders fields in fixed order`() {
        val formatted = VerificationFormatter.format(
            VerificationDetails(
                fileName = "demo.apk",
                hashes = listOf(
                    HashValue(HashAlgorithm.MD5, "md5"),
                    HashValue(HashAlgorithm.SHA1, "sha1"),
                    HashValue(HashAlgorithm.SHA256, "sha256"),
                    HashValue(HashAlgorithm.CRC32, "crc32"),
                ),
            ),
        )

        assertEquals(
            """
            文件名: demo.apk
            MD5: md5
            SHA1: sha1
            SHA256: sha256
            CRC32: crc32
            """.trimIndent(),
            formatted,
        )
    }

    @Test
    fun `renders only selected algorithms in requested order`() {
        val formatted = VerificationFormatter.format(
            VerificationDetails(
                fileName = "demo.apk",
                hashes = listOf(
                    HashValue(HashAlgorithm.SHA256, "aa"),
                    HashValue(HashAlgorithm.CRC32, "bb"),
                ),
            ),
        )

        assertEquals(
            """
            文件名: demo.apk
            SHA256: aa
            CRC32: bb
            """.trimIndent(),
            formatted,
        )
    }
}
