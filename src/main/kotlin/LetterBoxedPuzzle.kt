import java.util.*

class LetterBoxedSolutionChecker(
    private val puzzle: LetterBoxedPuzzle,
    private val dictionary: Dictionary,
) {

    private var remainingLetters: MutableSet<Char> = puzzle.sides.flatten().toSet().toMutableSet()

    init {
        reset()
    }

    fun reset() {
        remainingLetters = puzzle.sides.flatten().toSet().toMutableSet()
    }

    fun checkWord(word: String, initialLetter: Char? = null): CheckedWordResult {
        if (word.isEmpty() || !dictionary.validWord(word)) {
            return InvalidWord(word, "The given word is not in the dictionary")
        }
        if (initialLetter != null && !puzzle.tree.containsKey(initialLetter)) {
            return InvalidWord(word, "first letter '$initialLetter' not possible")
        }

        val letters: MutableList<Char> = word.toMutableList()
        if (initialLetter != null) {
            if (word.first() != initialLetter) {
                return InvalidWord(word, "first letter of '$word' must match initial letter $initialLetter")
            }
            letters.removeFirst()
        }

        var possibleLetters: Set<Char> = if (initialLetter != null) {
            puzzle.tree[initialLetter]!!
        } else {
            puzzle.sides.flatten().toSet()
        }
        letters.forEach { letter ->
            if (possibleLetters.contains(letter)) {
                possibleLetters = puzzle.tree[letter]!!
                remainingLetters.remove(letter)
            } else {
                return InvalidWord(word, "cannot form word '$word' given current puzzle")
            }
        }
        return ValidWord(word, remainingLetters)
    }

    /**
     * Returns an empty list if the puzzle is solved with the given words.
     */
    fun checkSolution(words: List<String>): PuzzleSolutionResult {
        var firstCharacter: Char? = null
        words.forEach { currentWord ->
            when (val wordResult = checkWord(currentWord, firstCharacter)) {
                is ValidWord -> {
                    firstCharacter = currentWord.last()
                }

                is InvalidWord -> {
                    return InvalidPuzzleSolution(words, wordResult.message)
                }
            }
        }
        return if (remainingLetters.isEmpty()) {
            ValidPuzzleSolution(words)
        } else {
            IncompletePuzzleSolution(words, remainingLetters)
        }
    }
}

class LetterBoxedPuzzle(
    val sides: List<Set<Char>>
) {

    val tree: TreeMap<Char, Set<Char>> = TreeMap()

    init {
        checkArguments()
        buildTree()
    }

    private fun checkArguments() {
        if (sides.isEmpty()) {
            throw IllegalArgumentException("cannot use an empty puzzle")
        }
        val sideSize = sides[0].size
        if (sideSize < 2) {
            throw IllegalArgumentException("sides must be at least of length 2")
        }
        if (sides.any { it.size != sideSize }) {
            throw IllegalArgumentException("all sides must be of equal length")
        }
    }

    private fun buildTree() {
        sides.forEach { letters ->
            letters.forEach { letter ->
                tree[letter] = sides.flatten().toSet() - letters
            }
        }
    }
}
