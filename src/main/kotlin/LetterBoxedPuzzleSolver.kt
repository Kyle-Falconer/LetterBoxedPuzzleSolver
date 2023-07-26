import kotlinx.coroutines.*

data class BoxedWords(val word: String, val remainingLetters: Set<Char>) : Comparable<BoxedWords> {
    override fun compareTo(other: BoxedWords): Int {
        return when {
            remainingLetters.size < other.remainingLetters.size -> -1
            remainingLetters.size > other.remainingLetters.size -> 1
            else -> {
                word.compareTo(other.word)
            }
        }
    }
}

class LetterBoxedPuzzleSolver(
    private val puzzle: LetterBoxedPuzzle,
    private val dictionary: Dictionary,
    private val threadCount: Int = 16
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val solverContext = Dispatchers.IO.limitedParallelism(threadCount)

    private var possibleWordPool = mutableListOf<BoxedWords>()

    fun findTopSolutions(stopEarly: Boolean = true): List<PuzzleSolutionResult> {
        possibleWordPool = findPossibleWords().sorted().toMutableList()

//        val partitionedPossibleWords = possibleWordPool.chunked(possibleWordPool.size / threadCount)
        // work on the top ~48 at a time
        val bufferSize = threadCount * 3
        var startIndex = 0
        var endIndex = bufferSize


        val deferredResults = mutableListOf<Deferred<Set<PuzzleSolutionResult?>>>()
        val solutions: MutableSet<PuzzleSolutionResult> = mutableSetOf()
        runBlocking {
            println("checking from indices $startIndex to $endIndex")
            val currentSubPool = possibleWordPool.subList(startIndex, endIndex)
            currentSubPool.forEach { dictWords ->
                deferredResults.add(async { checkWordsForSolution(dictWords) })
            }

            deferredResults.forEach { result ->
                result.await().let { solutionSet ->
                    solutionSet.filterIsInstance<ValidPuzzleSolution>().forEach {
                        solutions.add(it)
                    }
                }
            }

            if (stopEarly && solutions.any { result ->
                    result is ValidPuzzleSolution
                }) {
                println("exiting solution checker loop because a solution has been found")
                return@runBlocking
            }
            startIndex = endIndex
            endIndex += bufferSize
        }
        val validSolutions = solutions.filterIsInstance<ValidPuzzleSolution>().sorted()
        println("found ${validSolutions.size} valid solutions, top=${validSolutions.firstOrNull()?.words?.stringify()}")
        return validSolutions
    }

    private suspend fun checkWordsForSolution(boxedWords: BoxedWords): Set<PuzzleSolutionResult> =
        withContext(solverContext) {

            val checker = LetterBoxedSolutionChecker(puzzle, dictionary)


            val lastLetter = boxedWords.word.last()
            val possibleNextWords = possibleWordPool.filter { it.word.first() == lastLetter }

            var bestAlternateSolution: IncompletePuzzleSolution? = null
            val possibleValidSolutions = mutableSetOf<ValidPuzzleSolution>()

            // just in case
            if (boxedWords.remainingLetters.isEmpty()) {
                val maybeSingleWordSolution = checker.checkSolution(listOf(boxedWords.word))
                if (maybeSingleWordSolution is ValidPuzzleSolution) {
                    println("found a solution in one word: ${boxedWords.word}")
                    possibleValidSolutions.add(maybeSingleWordSolution)
                }
                checker.reset()
            }

            possibleNextWords.forEach { nextWord ->
                val wordList = listOf(boxedWords.word, nextWord.word)
                val slnResult = checker.checkSolution(wordList)
                when (slnResult) {
                    is ValidPuzzleSolution -> {
                        println("found solution with ${wordList.stringify()}")
                        possibleValidSolutions.add(slnResult)
                    }

                    is IncompletePuzzleSolution -> {
                        if (bestAlternateSolution == null
                            || slnResult.remainingLetters.size < bestAlternateSolution!!.remainingLetters.size) {
                            bestAlternateSolution = slnResult
                        }
                    }

                    else -> {}
                }
                checker.reset()
            }
            if (possibleValidSolutions.isNotEmpty()) {
                return@withContext possibleValidSolutions
            } else if (bestAlternateSolution != null) {
                return@withContext setOf(bestAlternateSolution!!)
            } else {
                return@withContext setOf()
            }
        }

    private fun findPossibleWords(): List<BoxedWords> {
        val partitionedWords = dictionary.words.chunked(dictionary.words.size / threadCount)

        val result = mutableSetOf<BoxedWords>()
        val deferredResults = mutableListOf<Deferred<Set<BoxedWords>>>()
        runBlocking {
            partitionedWords.forEach { dictWords ->
                deferredResults.add(async { findPossibleWordsPartitioned(dictWords) })
            }

            deferredResults.forEach { result.addAll(it.await()) }
        }

        val sortedResult = result.sorted()
        println("filtered the possible words down to ${sortedResult.size} from ${dictionary.words.size} dictionary words")
        return sortedResult
    }

    private suspend fun findPossibleWordsPartitioned(dictionaryWordsSection: List<String>):
            Set<BoxedWords> = withContext(solverContext) {
        val result = mutableSetOf<BoxedWords>()
        if (dictionaryWordsSection.isEmpty()) {
            return@withContext result
        }
        val checker = LetterBoxedSolutionChecker(puzzle, dictionary)
        dictionaryWordsSection.forEach { dWord ->
            when (val wordResult = checker.checkWord(dWord)) {
                is ValidWord -> {
                    result.add(BoxedWords(dWord, wordResult.remainingLetters))
                }

                is InvalidWord -> {
                    // do nothing
                }
            }
            checker.reset()
        }
        println(
            "finished checking from words \"${dictionaryWordsSection.first()}\" " +
                    "to \"${dictionaryWordsSection.last()}\", found ${result.size} valid words"
        )
        return@withContext result
    }
}