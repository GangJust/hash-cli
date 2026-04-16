package hashcli

import hashcli.hash.HashAlgorithm
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.StandardCharsets
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CliApplicationTest {
    @Test
    fun `returns non zero when arguments are missing`() {
        val stdout = ByteArrayOutputStream()
        val stderr = ByteArrayOutputStream()

        val exit = CliApplication().run(emptyList(), stdout.printStream(), stderr.printStream())

        assertEquals(1, exit)
        assertContains(stderr.asText(), "Usage:")
    }

    @Test
    fun `prints help and exits zero`() {
        val stdout = ByteArrayOutputStream()
        val stderr = ByteArrayOutputStream()

        val exit = CliApplication().run(listOf("--help"), stdout.printStream(), stderr.printStream())

        assertEquals(0, exit)
        assertContains(stdout.asText(), "Usage:")
        assertEquals("", stderr.asText())
    }

    @Test
    fun `prints version and exits zero`() {
        val stdout = ByteArrayOutputStream()
        val stderr = ByteArrayOutputStream()

        val exit = CliApplication().run(listOf("--version"), stdout.printStream(), stderr.printStream())

        assertEquals(0, exit)
        assertContains(stdout.asText(), "hash-cli $AppVersion")
        assertEquals("", stderr.asText())
    }

    @Test
    fun `returns non zero when input path does not exist`() {
        val stdout = ByteArrayOutputStream()
        val stderr = ByteArrayOutputStream()

        val exit = CliApplication().run(listOf("D:/missing/file.apk"), stdout.printStream(), stderr.printStream())

        assertEquals(1, exit)
        assertContains(stderr.asText(), "does not exist")
    }

    @Test
    fun `processes a single file and writes sibling report`() {
        val root = newTempDir("hash-cli-single")
        try {
            val input = File(root, "demo.apk")
            input.writeText("hello", StandardCharsets.UTF_8)
            val stdout = ByteArrayOutputStream()
            val stderr = ByteArrayOutputStream()

            val exit = CliApplication().run(listOf(input.toString()), stdout.printStream(), stderr.printStream())

            val output = File(root, "demo.apk_verification.txt")
            assertEquals(0, exit)
            assertTrue(output.exists())
            assertContains(output.readText(StandardCharsets.UTF_8), "文件名: demo.apk")
            assertContains(output.readText(StandardCharsets.UTF_8), "MD5:")
            assertContains(output.readText(StandardCharsets.UTF_8), "SHA1:")
            assertContains(output.readText(StandardCharsets.UTF_8), "SHA256:")
            assertContains(output.readText(StandardCharsets.UTF_8), "CRC32:")
            assertContains(stdout.asText(), output.absolutePath)
            assertEquals("", stderr.asText())
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun `directory mode recursively processes regular files and excludes verification outputs`() {
        val root = newTempDir("hash-cli-dir")
        try {
            File(root, "nested").mkdirs()
            File(root, "first.apk").writeText("first", StandardCharsets.UTF_8)
            File(File(root, "nested"), "second.bin").writeText("second", StandardCharsets.UTF_8)
            File(root, "skip.apk_verification.txt").writeText("already generated", StandardCharsets.UTF_8)

            val stdout = ByteArrayOutputStream()
            val stderr = ByteArrayOutputStream()

            val exit = CliApplication().run(listOf(root.toString()), stdout.printStream(), stderr.printStream())

            assertEquals(0, exit)
            assertTrue(File(root, "first.apk_verification.txt").exists())
            assertTrue(File(File(root, "nested"), "second.bin_verification.txt").exists())
            assertFalse(File(root, "skip.apk_verification.txt_verification.txt").exists())
            assertFalse(stdout.asText().contains("skip.apk_verification.txt_verification.txt"))
            assertEquals("", stderr.asText())
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun `overwrites existing verification file`() {
        val root = newTempDir("hash-cli-overwrite")
        try {
            val input = File(root, "demo.apk")
            val output = File(root, "demo.apk_verification.txt")
            input.writeText("fresh", StandardCharsets.UTF_8)
            output.writeText("stale", StandardCharsets.UTF_8)

            val exit = CliApplication().run(listOf(input.toString()), ByteArrayOutputStream().printStream(), ByteArrayOutputStream().printStream())

            assertEquals(0, exit)
            assertContains(output.readText(StandardCharsets.UTF_8), "文件名: demo.apk")
            assertFalse(output.readText(StandardCharsets.UTF_8).contains("stale"))
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun `fails when no eligible source files are found`() {
        val root = newTempDir("hash-cli-empty")
        try {
            val onlyVerification = File(root, "demo.apk_verification.txt")
            onlyVerification.writeText("existing", StandardCharsets.UTF_8)
            val stderr = ByteArrayOutputStream()

            val exit = CliApplication().run(listOf(root.toString()), ByteArrayOutputStream().printStream(), stderr.printStream())

            assertEquals(1, exit)
            assertContains(stderr.asText(), "No eligible source files found")
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun `fails fast when a collected file cannot be processed`() {
        val root = newTempDir("hash-cli-fail-fast")
        try {
            val first = File(root, "first.apk")
            val second = File(root, "second.apk")
            first.writeText("ok", Charsets.UTF_8)
            second.writeText("bad", Charsets.UTF_8)
            val service = VerificationService(
                hashCalculator = object : HashCalculator {
                    override fun calculate(path: File, algorithms: List<HashAlgorithm>, uppercase: Boolean): VerificationDetails {
                        if (path.name == "second.apk") {
                            throw IllegalStateException("boom")
                        }
                        return VerificationDetails(
                            fileName = "first.apk",
                            hashes = listOf(),
                        )
                    }
                },
            )
            val stderr = ByteArrayOutputStream()

            val exit = CliApplication(service).run(listOf(root.toString()), ByteArrayOutputStream().printStream(), stderr.printStream())

            assertEquals(1, exit)
            assertContains(stderr.asText(), "boom")
            assertTrue(File(root, "first.apk_verification.txt").exists())
            assertFalse(File(root, "second.apk_verification.txt").exists())
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun `filters algorithms and uppercases output values`() {
        val root = newTempDir("hash-cli-algorithms")
        try {
            val input = File(root, "demo.apk")
            input.writeText("hello", StandardCharsets.UTF_8)

            val exit = CliApplication().run(
                listOf(input.toString(), "--algorithm", "sha1", "--algorithm", "md5", "--uppercase"),
                ByteArrayOutputStream().printStream(),
                ByteArrayOutputStream().printStream(),
            )

            val output = File(root, "demo.apk_verification.txt").readText(StandardCharsets.UTF_8)
            assertEquals(0, exit)
            assertContains(output, "SHA1: AAF4C61DDCC5E8A2DABEDE0F3B482CD9AEA9434D")
            assertContains(output, "MD5: 5D41402ABC4B2A76B9719D911017C592")
            assertFalse(output.contains("SHA256:"))
            assertFalse(output.contains("CRC32:"))
            assertTrue(output.indexOf("SHA1:") < output.indexOf("MD5:"))
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun `returns non zero for unsupported algorithm`() {
        val stdout = ByteArrayOutputStream()
        val stderr = ByteArrayOutputStream()

        val exit = CliApplication().run(
            listOf("demo.apk", "--algorithm", "sha512"),
            stdout.printStream(),
            stderr.printStream(),
        )

        assertEquals(1, exit)
        assertContains(stderr.asText(), "Unsupported algorithm")
        assertEquals("", stdout.asText())
    }
}
