package hashcli.hash

import hashcli.newTempDir
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.charset.StandardCharsets
import kotlin.test.Test
import kotlin.test.assertEquals

class FileHashUtilsTest {
    @Test
    fun `calculates selected algorithms in requested order`() {
        val root = newTempDir("hash-cli-hashutils")
        try {
            val input = File(root, "demo.apk")
            input.writeText("hello", StandardCharsets.UTF_8)

            val result = FileHashUtils.calculate(
                path = input,
                algorithms = listOf(HashAlgorithm.SHA1, HashAlgorithm.CRC32),
                uppercase = true,
            )

            assertEquals(
                listOf(
                    HashValue(HashAlgorithm.SHA1, "AAF4C61DDCC5E8A2DABEDE0F3B482CD9AEA9434D"),
                    HashValue(HashAlgorithm.CRC32, "3610A686"),
                ),
                result,
            )
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun `calculates hashes from input stream for android style reuse`() {
        val result = ByteArrayInputStream("hello".toByteArray(StandardCharsets.UTF_8)).use { input ->
            FileHashUtils.calculate(
                input = input,
                algorithms = listOf(HashAlgorithm.MD5, HashAlgorithm.SHA256),
                uppercase = false,
            )
        }

        assertEquals(
            listOf(
                HashValue(HashAlgorithm.MD5, "5d41402abc4b2a76b9719d911017c592"),
                HashValue(HashAlgorithm.SHA256, "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824"),
            ),
            result,
        )
    }
}
