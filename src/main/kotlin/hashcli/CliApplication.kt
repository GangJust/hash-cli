package hashcli

import java.io.PrintStream
import java.io.File

const val AppVersion = "1.0.0"

class CliApplication(
    private val service: VerificationService = VerificationService(),
    private val parser: CliArgumentParser = CliArgumentParser(),
) {
    fun run(args: List<String>, out: PrintStream, err: PrintStream): Int {
        return when (val parsed = parser.parse(args)) {
            is CliParseResult.Run -> runWithConfig(parsed.config, out, err)
            is CliParseResult.WriteStdout -> {
                out.println(parsed.message)
                parsed.exitCode
            }

            is CliParseResult.WriteStderr -> {
                err.println(parsed.message)
                parsed.exitCode
            }
        }
    }

    private fun runWithConfig(config: CliConfig, out: PrintStream, err: PrintStream): Int {
        val input = config.input
        if (!input.exists() || !input.canRead()) {
            err.println("Input path does not exist or is not readable: $input")
            return 1
        }

        return try {
            val generated = service.process(config)
            generated.forEach { out.println(it.absolutePath) }
            0
        } catch (ex: IllegalArgumentException) {
            err.println(ex.message ?: "Invalid input")
            1
        } catch (ex: Exception) {
            err.println(ex.message ?: "Processing failed")
            1
        }
    }
}

data class CliConfig(
    val input: File,
    val algorithms: List<hashcli.hash.HashAlgorithm> = hashcli.hash.HashAlgorithm.defaultSelection(),
    val uppercase: Boolean = false,
)
