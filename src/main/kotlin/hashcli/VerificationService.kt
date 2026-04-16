package hashcli

import hashcli.hash.FileHashUtils
import hashcli.hash.HashAlgorithm
import hashcli.hash.HashValue
import java.io.File
import java.nio.charset.StandardCharsets

class VerificationService(
    private val hashCalculator: HashCalculator = StreamingHashCalculator(),
    private val formatter: VerificationFormatter = VerificationFormatter,
) {
    fun process(config: CliConfig): List<File> {
        val sources = collectSources(config.input)
        return sources.map { source ->
            val details = hashCalculator.calculate(source, config.algorithms, config.uppercase)
            val output = SourceFilePolicy.outputPathFor(source)
            output.writeText(formatter.format(details), StandardCharsets.UTF_8)
            output
        }
    }

    private fun collectSources(input: File): List<File> {
        return when {
            input.isFile -> listOf(input).filterEligible()
            input.isDirectory -> input.walkTopDown().filter { it.isFile }.toList().filterEligible()

            else -> throw IllegalArgumentException("Input path must be a readable file or directory: $input")
        }
    }

    private fun List<File>.filterEligible(): List<File> {
        val filtered = filter(SourceFilePolicy::isEligibleSource)
        if (filtered.isEmpty()) {
            throw IllegalArgumentException("No eligible source files found for input.")
        }
        return filtered
    }
}

interface HashCalculator {
    fun calculate(path: File, algorithms: List<HashAlgorithm>, uppercase: Boolean): VerificationDetails
}

class StreamingHashCalculator : HashCalculator {
    override fun calculate(path: File, algorithms: List<HashAlgorithm>, uppercase: Boolean): VerificationDetails {
        return VerificationDetails(
            fileName = path.name,
            hashes = FileHashUtils.calculate(path, algorithms, uppercase),
        )
    }
}

data class VerificationDetails(
    val fileName: String,
    val hashes: List<HashValue>,
)
