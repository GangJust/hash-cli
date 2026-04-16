package hashcli

import hashcli.hash.HashAlgorithm
import java.io.File

sealed interface CliParseResult {
    data class Run(val config: CliConfig) : CliParseResult
    data class WriteStdout(val message: String, val exitCode: Int) : CliParseResult
    data class WriteStderr(val message: String, val exitCode: Int) : CliParseResult
}

class CliArgumentParser {
    fun parse(args: List<String>): CliParseResult {
        if (args.isEmpty()) {
            return CliParseResult.WriteStderr(usage(), 1)
        }

        var inputPath: String? = null
        val algorithms = linkedSetOf<HashAlgorithm>()
        var uppercase = false
        var index = 0

        while (index < args.size) {
            when (val arg = args[index]) {
                "--help", "-h" -> return CliParseResult.WriteStdout(usage(), 0)
                "--version", "-V" -> return CliParseResult.WriteStdout("hash-cli $AppVersion", 0)
                "--uppercase", "-u" -> {
                    uppercase = true
                    index += 1
                }

                "--algorithm", "-a" -> {
                    val value = args.getOrNull(index + 1)
                        ?: return CliParseResult.WriteStderr("Missing value for $arg\n${usage()}", 1)
                    val algorithm = HashAlgorithm.parse(value)
                        ?: return CliParseResult.WriteStderr("Unsupported algorithm: $value\n${usage()}", 1)
                    algorithms += algorithm
                    index += 2
                }

                else -> {
                    if (arg.startsWith("-")) {
                        return CliParseResult.WriteStderr("Unknown option: $arg\n${usage()}", 1)
                    }
                    if (inputPath != null) {
                        return CliParseResult.WriteStderr("Only one input path is allowed.\n${usage()}", 1)
                    }
                    inputPath = arg
                    index += 1
                }
            }
        }

        val resolvedInput = inputPath
            ?: return CliParseResult.WriteStderr("Input path is required.\n${usage()}", 1)

        return CliParseResult.Run(
            CliConfig(
                input = File(resolvedInput),
                algorithms = algorithms.toList().ifEmpty(HashAlgorithm::defaultSelection),
                uppercase = uppercase,
            ),
        )
    }

    fun usage(): String = buildString {
        appendLine("Usage: java -jar hash-cli.jar <file-or-directory-path> [options]")
        appendLine()
        appendLine("Options:")
        appendLine("  -a, --algorithm <md5|sha1|sha256|crc32>  Select algorithms, repeatable")
        appendLine("  -u, --uppercase                          Output hash values in uppercase")
        appendLine("  -h, --help                               Show help")
        appendLine("  -V, --version                            Show version")
        appendLine()
        appendLine("Defaults:")
        appendLine("  Algorithms: md5, sha1, sha256, crc32")
        appendLine()
        appendLine("Examples:")
        appendLine("  java -jar hash-cli.jar demo.apk")
        appendLine("  java -jar hash-cli.jar demo.apk --uppercase")
        appendLine("  java -jar hash-cli.jar demo.apk -a md5 -a sha1")
    }.trimEnd()
}
