import utils.CharGrid
import utils.Position
import utils.readInput
import utils.verify

fun main() {
    val testInput = readInput("Day21_test")
    verify(126384, Day21.part1(testInput))
    println("Test input OK")
    val input = readInput("Day21")
    println(Day21.part1(input))
    verify(0, Day21.part2(testInput))
    println(Day21.part2(input))
}

object Day21 {
    enum class Directional(val distance: Int) {
        UP(1), RIGHT(1), DOWN(2), LEFT(3), ACTION(0);

        companion object {
            operator fun get(char: Char) = when (char) {
                '^' -> UP
                '>' -> RIGHT
                'v' -> DOWN
                '<' -> LEFT
                'A' -> ACTION
                else -> error("Unsupported: $char")
            }
        }
    }

    private val numericKeypad = CharGrid("789\n456\n123\nX0A").nonEmptyCells.associate { (pos, char) -> char to pos }
    private val directionalKeypad =
        CharGrid("X^A\n<v>").nonEmptyCells.filter { it.second != 'X' }
            .associate { (pos, char) -> Directional[char] to pos }

    fun part1(input: String): Int {
        val solution = input.lines().associateWith { findMySequenceLength(it) }
        println(solution)
        return solution.map { (code, length) ->
            check(code.endsWith('A'))
            val numericPart = code.dropLast(1)
            check(numericPart.all { it.isDigit() })
            numericPart.toInt(10) * length
        }.sum()
    }

    private fun findMySequenceLength(target: String): Int {
        println(target)
        val moves = ("A$target").map { numericKeypad.getValue(it) }.zipWithNext()
        println(moves)
        val best = findSequenceInNumeric(moves).minOf { sequences ->
            println("depressurized: $sequences")
            val moreMoves = findMoves(sequences)
            println("$moreMoves")
            findSequenceInDirectional(moreMoves).minOf { radiationSequences ->
                println("radiation: ${radiationSequences.size} $radiationSequences")
                findSequenceInDirectional(findMoves(radiationSequences)).minOf { coldSequences ->
                    println("cold: ${coldSequences.size} $coldSequences")
                    coldSequences.size
                }
            }
        }
        return best
    }

    private fun findMoves(moves: List<Directional>): List<Pair<Position, Position>> =
        (listOf(Directional.ACTION) + moves).map { directionalKeypad.getValue(it) }.zipWithNext()

    private fun findSequenceInNumeric(moves: List<Pair<Position, Position>>): Sequence<List<Directional>> {
        val options = moves.map { (from, to) ->
            val hMoves = hMoves(from, to)
            val vMoves = vMoves(from, to)
            buildList {
                if (from.row != 3 || to.column != 0)
                    add((hMoves + vMoves) + Directional.ACTION)
                if (to.row != 3 || from.column != 0)
                    add((vMoves + hMoves) + Directional.ACTION)
            }.distinct()
        }
        check(options.all { it.isNotEmpty() })
        return sequencesFor(options, 0, emptyList()).map { it.flatten() }
    }

    private fun findSequenceInDirectional(moves: List<Pair<Position, Position>>): Sequence<List<Directional>> {
        val options = moves.map { (from, to) ->
            if (from == to)
                listOf(listOf(Directional.ACTION))
            else {
                val hMoves = hMoves(from, to)
                val vMoves = vMoves(from, to)
                buildList {
                    if (to != directionalKeypad[Directional.LEFT])
                        add((hMoves + vMoves) + Directional.ACTION)
                    if (from != directionalKeypad[Directional.LEFT])
                        add((vMoves + hMoves) + Directional.ACTION)
                }.distinct()
            }
        }
        check(options.all { it.isNotEmpty() }) { "No moves found for $moves" }
        return sequencesFor(options, 0, emptyList()).map { it.flatten() }
    }

    private fun <E> sequencesFor(lists: List<List<E>>, index: Int, active: List<E>): Sequence<List<E>> {
        if (index >= lists.size) return sequenceOf(active)
        val cur = lists[index]
        if (cur.size <= 1)
            return sequencesFor(lists, index + 1, active + cur.single())
        return cur.asSequence().flatMap { option ->
            sequencesFor(lists, index + 1, active + option)
        }
    }

    private fun hMoves(from: Position, to: Position) = when {
        from.column < to.column -> List(to.column - from.column) { Directional.RIGHT }
        else -> List(from.column - to.column) { Directional.LEFT }
    }

    private fun vMoves(from: Position, to: Position) = when {
        from.row < to.row -> List(to.row - from.row) { Directional.DOWN }
        else -> List(from.row - to.row) { Directional.UP }
    }

    fun part2(input: String): Int {
        return 0
        // TODO("Not yet implemented")
    }


}
