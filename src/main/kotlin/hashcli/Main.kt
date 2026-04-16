package hashcli

fun main(args: Array<String>) {
    val exitCode = CliApplication().run(args.toList(), System.out, System.err)
    if (exitCode != 0) {
        kotlin.system.exitProcess(exitCode)
    }
}
