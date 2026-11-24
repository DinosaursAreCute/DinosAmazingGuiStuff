import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

data class ShellResult(
    val stdout: String,
    val stderr: String,
    val exitCode: Int,
    val timedOut: Boolean = false,
) {
    val isSuccess: Boolean get() = exitCode == 0 && !timedOut
}

object Shell {

    /** Shell used when none is passed. Change it globally, e.g. Shell.defaultShell = listOf("/bin/bash", "-c"). */
    var defaultShell: List<String> =
        if (System.getProperty("os.name").startsWith("Windows", ignoreCase = true))
            listOf("cmd.exe", "/c")
        else
            listOf("/bin/sh", "-c")

    /** Runs a command string through a shell, so pipes, globs and && all work. */
    fun run(
        command: String,
        shell: List<String> = defaultShell,
        workingDir: File? = null,
        env: Map<String, String> = emptyMap(),
        timeoutMillis: Long? = null,
    ): ShellResult = execute(shell + command, workingDir, env, timeoutMillis)

    /** Runs a program with arguments directly (no shell parsing, so no injection or quoting issues). */
    fun run(
        command: List<String>,
        workingDir: File? = null,
        env: Map<String, String> = emptyMap(),
        timeoutMillis: Long? = null,
    ): ShellResult = execute(command, workingDir, env, timeoutMillis)

    fun run(
        command: Array<String>,
        workingDir: File? = null,
        env: Map<String, String> = emptyMap(),
        timeoutMillis: Long? = null,
    ): ShellResult = execute(command.toList(), workingDir, env, timeoutMillis)

    private fun execute(
        command: List<String>,
        workingDir: File?,
        env: Map<String, String>,
        timeoutMillis: Long?,
    ): ShellResult {
        val builder = ProcessBuilder(command).directory(workingDir)
        builder.environment().putAll(env)

        val process = try {
            builder.start()
        } catch (e: IOException) {
            return ShellResult("", e.message ?: "Failed to start process", -1)
        }
        process.outputStream.close() // no stdin, so commands waiting for input don't hang

        // Read both streams at the same time. Reading one after the other can deadlock
        // when the unread pipe's buffer fills up.
        val out = StringBuilder()
        val err = StringBuilder()
        val outReader = thread { process.inputStream.bufferedReader().use { out.append(it.readText()) } }
        val errReader = thread { process.errorStream.bufferedReader().use { err.append(it.readText()) } }

        val finished = if (timeoutMillis == null) {
            process.waitFor(); true
        } else {
            process.waitFor(timeoutMillis, TimeUnit.MILLISECONDS)
        }

        if (!finished) {
            process.descendants().forEach { it.destroyForcibly() } // kill children too, or the pipes stay open
            process.destroyForcibly()
            process.waitFor()
        }
        outReader.join()
        errReader.join()

        return ShellResult(
            stdout = out.toString(),
            stderr = err.toString(),
            exitCode = if (finished) process.exitValue() else -1,
            timedOut = !finished,
        )
    }
}