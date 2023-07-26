import okio.*
import okio.Path.Companion.toPath
import java.io.InputStream
import kotlin.io.use

class Dictionary {

    val words: List<String>

    init {
//        val rawWords = sanitized()
//        writeDictionary("sanitized_words_list.txt", rawWords)
        words = readDictionary("sanitized_words_list.txt")
    }

    fun validWord(word: String): Boolean {
        return words.contains(word)
    }

    private fun readDictionary(resourceFileName: String): List<String> {
        val dictionaryResource = this.javaClass.classLoader
            .getResourceAsStream(resourceFileName)
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
            dictionaryText.lines().forEach { line ->
                val word = line.trim()
                word.isNotEmpty().let {
                    result.add(word)
                }
            }
        }
        return result.toList()
    }

    @Throws(IOException::class)
    fun writeDictionary(outputFileName:String, words:List<String>) {
        val resourcePath = outputFileName.toPath()

        FileSystem.SYSTEM.write(resourcePath) {
            words.forEach {
                writeUtf8(it)
                writeUtf8("\n")
            }
        }
    }

    private fun sanitized(): List<String> {
        val sanitized = mutableSetOf<String>()
        readDictionary("raw_words_list.txt").forEach { word -> sanitized.add(word) }
        return sanitized
            .asSequence()
            .filter {
                // keep only alphabetic words
                !it.contains("""[0-9.&'/\-]""".toRegex())
            }
            .filter {
                // keep only words 3+ characters long
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