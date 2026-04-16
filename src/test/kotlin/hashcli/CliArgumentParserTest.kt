package hashcli

import hashcli.hash.HashAlgorithm
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class CliArgumentParserTest {
    private val parser = CliArgumentParser()

    @Test
    fun `uses default algorithms when none are specified`() {
        val result = assertIs<CliParseResult.Run>(parser.parse(listOf("demo.apk")))

        assertEquals("demo.apk", result.config.input.path)
        assertEquals(HashAlgorithm.defaultSelection(), result.config.algorithms)
        assertEquals(false, result.config.uppercase)
    }

    @Test
    fun `keeps repeated algorithm order and uppercase flag`() {
        val result = assertIs<CliParseResult.Run>(
            parser.parse(listOf("demo.apk", "--algorithm", "sha1", "--algorithm", "md5", "--uppercase")),
        )

        assertEquals(listOf(HashAlgorithm.SHA1, HashAlgorithm.MD5), result.config.algorithms)
        assertTrue(result.config.uppercase)
    }
}
