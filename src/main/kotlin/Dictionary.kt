import okio.*
import okio.Path.Companion.toPath
import java.io.InputStream
import java.net.URL
import kotlin.io.use

class Dictionary {

    val words: List<String>

    init {
        words = readSanitizedDictionary()
    }

    fun validWord(word: String): Boolean {
        return words.contains(word)
    }

    private fun readSanitizedDictionary(): List<String> {
        val dictionaryResource: InputStream? = this.javaClass.classLoader.getResourceAsStream("sanitized_words_list.txt")
//        val resourcePath = dictionaryResource?.path?.toPath()
        return readDictionary(dictionaryResource)
    }

    private fun readDictionary(inputStream: InputStream?): List<String> {
        if (inputStream == null) {
            println("cannot load dictionary")
            return listOf()
        }
        val result = mutableListOf<String>()

        inputStream.bufferedReader().use {
            val dictionaryText = it.readText()
            dictionaryText.lines().forEach{line ->
                val word = line.trim()
                word.isNotEmpty().let {
                    result.add(word)
                }
            }
        }
//        FileSystem.SYSTEM.source(path).use { fileSource ->
//            fileSource.buffer().use { bufferedFileSource ->
//                while (true) {
//                    val line = bufferedFileSource.readUtf8Line() ?: break
//                    result.add(line.trim())
//                }
//            }
//        }
        return result.toList()
    }


    private fun readRawDictionary(): List<String> {
        val dictionaryResource = this.javaClass.classLoader
            .getResource("raw_words_list.txt")
        val resourcePath = dictionaryResource?.path?.toPath()
        return readDictionary(resourcePath)
    }

    private fun readDictionary(path: Path?): List<String> {
        if (path == null) {
            println("cannot load dictionary")
            return listOf()
        }
        val result = mutableListOf<String>()
        FileSystem.SYSTEM.source(path).use { fileSource ->
            fileSource.buffer().use { bufferedFileSource ->
                while (true) {
                    val line = bufferedFileSource.readUtf8Line() ?: break
                    result.add(line.trim())
                }
            }
        }
        return result.toList()
    }


    @Throws(IOException::class)
    fun writeSanitized() {
        val resourcePath = "sanitized_words_list.txt".toPath()

        val sanitizedList = sanitized()
        FileSystem.SYSTEM.write(resourcePath) {
            sanitizedList.forEach {
                writeUtf8(it)
                writeUtf8("\n")
            }
        }
    }

    private fun sanitized(): List<String> {
        val sanitized = mutableSetOf<String>()
        readRawDictionary().forEach { word -> sanitized.add(word) }
        return sanitized
            .asSequence()
            .filter {
                !it.contains("""[0-9.&'/\-]""".toRegex())
            }
            .filter {
                //anything less than 2 characters long
                it.length >= 2
            }
            .filter {
                // remove any that are all caps
                it.uppercase() != it
            }
            .filter { word ->
                word.count { c -> Character.isUpperCase(c) } == 0
            }
            .sorted()
            .toList()
    }
}