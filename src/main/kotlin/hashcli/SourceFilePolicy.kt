package hashcli

import java.io.File

object SourceFilePolicy {
    private const val VerificationSuffix = "_verification.txt"

    fun isEligibleSource(path: File): Boolean = !path.name.endsWith(VerificationSuffix)

    fun outputPathFor(source: File): File {
        require(source.isFile) { "Source path must be a file: $source" }
        val parent = source.parentFile ?: File(".")
        return File(parent, "${source.name}$VerificationSuffix")
    }
}
