import java.io.File
import java.lang.RuntimeException

fun main() {
    val entry = readFile(".")
    entry.print("")
}

sealed interface Entry
data class SingleFile(val name: String, val size: Long): Entry
data class Directory(val name: String, val files: List<Entry>): Entry

fun Entry.print(indent: String) {
    when (this) {
        is SingleFile -> println("$indent ${this.name} ${this.size}")
        is Directory -> {
            val length = this.length()
            println("$indent ${this.name} $length")
            this.files.forEach { file -> file.print("$indent    ")}
        }
    }
}

fun Entry.length(): Long {
    return when (this) {
        is SingleFile -> this.size
        is Directory -> {
            this.files.fold(0L) { acc, entry ->
                acc + when (entry) {
                    is SingleFile -> entry.size
                    is Directory -> entry.length()
                }
            }
        }
    }
}

fun readFile(path: String): Entry {
    val entry = File(path)
    if (entry.isDirectory) {
        val entries = entry.listFiles()?.fold(emptyList<Entry>()) { acc, file ->
            val subEntry = readFile(file.path)
            return@fold acc + subEntry
        }
        return Directory(entry.name, entries ?: emptyList())
    } else if (entry.isFile) {
        return SingleFile(entry.name, entry.length())
    }
    throw RuntimeException("Don't know how to process $entry")
}
