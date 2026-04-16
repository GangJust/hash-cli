package hashcli

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SourceFilePolicyTest {
    @Test
    fun `derives sibling verification file name`() {
        val root = newTempDir("hash-cli-policy")
        try {
            val source = File.createTempFile("demo", ".apk", root)
            assertEquals("${source.name}_verification.txt", SourceFilePolicy.outputPathFor(source).name)
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun `rejects verification files as source inputs`() {
        assertFalse(SourceFilePolicy.isEligibleSource(File("demo.apk_verification.txt")))
        assertTrue(SourceFilePolicy.isEligibleSource(File("demo.apk")))
    }
}
