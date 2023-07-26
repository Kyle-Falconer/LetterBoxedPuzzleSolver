sealed class PuzzleSolutionResult(val words: List<String>) {
    fun print() {
        when (this) {
            is ValidPuzzleSolution -> {
                println("${words.stringify()} ✅ valid solution in ${words.size} words")
            }

            is IncompletePuzzleSolution -> {
                println("${words.stringify()} ❌ incomplete solution; remaining letters= $remainingLetters")
            }

            is InvalidPuzzleSolution -> {
                println("${words.stringify()} ❌ invalid solution, $message")
            }
        }
    }
}

class ValidPuzzleSolution(words: List<String>) : PuzzleSolutionResult(words), Comparable<ValidPuzzleSolution> {
    private val solutionCharacterCount = words.joinToString().length
    private fun countCharacterOverlap(): Int {
        return solutionCharacterCount - words.joinToString().toSet().size
    }

    override fun compareTo(other: ValidPuzzleSolution): Int {
        return if (this.countCharacterOverlap() < other.countCharacterOverlap()) {
            -1
        } else if (this.countCharacterOverlap() > other.countCharacterOverlap()) {
            1
        } else {
            solutionCharacterCount.compareTo(other.solutionCharacterCount)
        }
    }
}

class IncompletePuzzleSolution(words: List<String>, val remainingLetters: Set<Char>) : PuzzleSolutionResult(words)
class InvalidPuzzleSolution(words: List<String>, val message: String) : PuzzleSolutionResult(words)
sealed class CheckedWordResult(val word: String) {
    fun print() {
        when (this) {
            is ValidWord -> {
                println("\"$word\" ✅ valid word; remaining letters= $remainingLetters")
            }

            is InvalidWord -> {
                println("\"$word\" ❌ invalid word: $message")
            }
        }
    }
}

class ValidWord(word: String, val remainingLetters: Set<Char>) : CheckedWordResult(word)
class InvalidWord(word: String, val message: String) : CheckedWordResult(word)
