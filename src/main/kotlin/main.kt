import java.io.File
import java.lang.RuntimeException
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.ForkJoinTask
import java.util.concurrent.RecursiveTask
import kotlin.streams.toList

fun main() {
    val commonPool = ForkJoinPool.commonPool()
    val readFileTask = ReadFileTask(File("."))
    commonPool.invoke(readFileTask)
    val result = readFileTask.join()
    result.print("")
}

sealed interface Entry
data class SingleFile(val name: String, val size: Long): Entry
data class Directory(val name: String, val size: Long, val entries: List<Entry>): Entry

fun Entry.print(indent: String) {
    when (this) {
        is SingleFile -> println("$indent ${this.name} ${this.size}")
        is Directory -> {
            val length = this.length()
            println("$indent ${this.name} $length")
            this.entries.forEach { file -> file.print("$indent    ")}
        }
    }
}

fun Entry.length(): Long {
    return when (this) {
        is SingleFile -> this.size
        is Directory -> {
            this.entries.fold(0L) { acc, entry ->
                acc + when (entry) {
                    is SingleFile -> entry.size
                    is Directory -> entry.length()
                }
            }
        }
    }
}

class ReadFileTask(private val file: File) : RecursiveTask<Entry>() {
    override fun compute(): Entry {
        val subtasks = createSubtasks()
        if (file.isFile) {
            return process()
        } else if (file.isDirectory) {
            val entries = ForkJoinTask.invokeAll(subtasks).stream().map { task -> task.join() }.toList()
            return Directory(file.name, entries.sumOf { it.length() }, entries)
        }
        throw RuntimeException("Don't know how to handle $file")
    }

    private fun createSubtasks(): List<ReadFileTask> =
        if (file.isDirectory) {
            file.listFiles()?.fold(emptyList()) { acc, file ->
                acc + ReadFileTask(file)
            } ?: emptyList()
        } else if (file.isFile) {
            listOf(ReadFileTask(file))
        } else {
            emptyList()
        }

    private fun process(): Entry = SingleFile(file.name, file.length())
}
