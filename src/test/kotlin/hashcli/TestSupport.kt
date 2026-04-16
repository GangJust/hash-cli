package hashcli

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

fun newTempDir(prefix: String): File {
    val dir = File(System.getProperty("java.io.tmpdir"), "$prefix-${System.nanoTime()}")
    check(dir.mkdirs()) { "Failed to create temp dir: $dir" }
    return dir
}

fun ByteArrayOutputStream.asText(charset: Charset = StandardCharsets.UTF_8): String = toString(charset)

fun ByteArrayOutputStream.printStream(charset: Charset = StandardCharsets.UTF_8): PrintStream =
    PrintStream(this, true, charset)
