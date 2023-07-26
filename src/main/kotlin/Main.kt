fun main(args: Array<String>) {
    val dictionary = Dictionary()
    println("loaded ${dictionary.words.size} words into the dictionary")


    val puzzle1Checker = buildAndSolve(
        listOf(
            setOf('y', 't', 's'),
            setOf('b', 'n', 'a'),
            setOf('c', 'o', 'r'),
            setOf('e', 'i', 'f'),
        ), dictionary
    )
    val sln1Result =
        puzzle1Checker.checkSolution(listOf("cybernetics", "sofa"))    // this is the NYT solution for that puzzle
    sln1Result.print()

    val puzzle2Checker = buildAndSolve(
        listOf(
            setOf('t', 'a', 'm'),
            setOf('u', 'r', 'd'),
            setOf('q', 'b', 'i'),
            setOf('o', 'n', 'e'),
        ), dictionary
    )
    val sln2Result =
        puzzle2Checker.checkSolution(listOf("quotidian", "number"))    // this is the NYT solution for that puzzle
    sln2Result.print()

    val puzzle3Checker = buildAndSolve(
        listOf(
            setOf('i', 'l', 'p'),
            setOf('u', 'r', 'n'),
            setOf('s', 'o', 'a'),
            setOf('h', 't', 'd'),
        ), dictionary
    )
    val sln3Result = puzzle3Checker.checkSolution(listOf("outlandish", "harp"))
    sln3Result.print()
}

fun buildAndSolve(sides: List<Set<Char>>, dictionary: Dictionary): LetterBoxedSolutionChecker {
    val puzzle2 = LetterBoxedPuzzle(sides)
    val puzzleChecker = LetterBoxedSolutionChecker(puzzle2, dictionary)
    val solver = LetterBoxedPuzzleSolver(puzzle2, dictionary)
    val words = solver.findTopSolutions(stopEarly = false)
    puzzleChecker.reset()
    val solverSolutionResult = puzzleChecker.checkSolution(words.first().words)
    solverSolutionResult.print()
    return puzzleChecker
}