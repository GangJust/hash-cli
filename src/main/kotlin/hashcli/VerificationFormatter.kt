package hashcli

object VerificationFormatter {
    fun format(details: VerificationDetails): String {
        return buildString {
            appendLine("文件名: ${details.fileName}")
            details.hashes.forEachIndexed { index, hash ->
                val line = "${hash.algorithm.displayName}: ${hash.value}"
                if (index == details.hashes.lastIndex) {
                    append(line)
                } else {
                    appendLine(line)
                }
            }
        }
    }
}
