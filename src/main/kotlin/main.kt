import java.io.File
import java.lang.RuntimeException

fun main() {
    readFile(".", "")
}

fun readFile(path: String, indent: String): Long? {
    val entry = File(path)
    if (entry.isDirectory) {
        val directorySize = entry.listFiles()?.fold(0L) { acc, file ->
            val fileSize = readFile(file.path, "$indent    ")
            if (fileSize != null) {
                return@fold acc + fileSize
            }
            return@fold acc
        }
        println("$indent ${entry.name} $directorySize")
        return directorySize
    } else if (entry.isFile) {
        println("$indent ${entry.name} ${entry.length()}")
        return entry.length()
    }
    throw RuntimeException("Don't know how to process $entry")
}
