import utils.*

fun main() {
    val testInput = readInput("Day18_test")
    verify(22, Day18.part1(testInput, 12, 7))
    println("Test input OK")
    val input = readInput("Day18")
    println(Day18.part1(input, 1024, 71))
    verify("6,1", Day18.part2(testInput, 7))
    println("Test input OK")
    println(Day18.part2(input, 71))
}

object Day18 {
    fun part1(input: String, time: Int, size: Int): Long {
        val positions: List<Position> = input.lines().map { it.intsOrZero() }.map { (x, y) -> Position(y, x) }
        val grid = ByteGrid(positions.take(time), size, size)
        val path = requireNotNull(grid.findPath()) { "No path for $time" }
        return path.score.toLong()
    }

    fun part2(input: String, size: Int): String {
        val positions: List<Position> = input.lines().map { it.intsOrZero() }.map { (x, y) -> Position(y, x) }
        var time = size
        while (true) {
            val activeBlocks = positions.take(time++)
            val grid = ByteGrid(activeBlocks, size, size)
            val path = grid.findPath() ?: return activeBlocks.last().let { it.column.toString() + "," + it.row }
//            println("$time: " + path.score)
        }
    }

    data class Path(val score: Int, val positions: Set<Position>)

    class ByteGrid(positions: List<Position>, val height: Int, val width: Int) :
        OpenGrid<Boolean>(positions.associateWith { true }, false, height, width) {

        fun findPath(): Path? {
            val grid = this
            val startingPosition = Position(0, 0)
            val start = startingPosition
            val endPoint = Position(height - 1, width - 1)
            val steps = mutableMapOf(start to Path(0, setOf(startingPosition)))
            val visited = mutableSetOf<Position>()
            while (steps.isNotEmpty()) {
                val (step, path) = steps.minBy { it.value.score }
                if (step == endPoint) return path
                if (step !in grid) continue
                visited.add(step)
                steps.remove(step)
                Direction.entries.forEach { dir ->
                    val nextStep = step + dir
                    if (nextStep in grid && !grid[nextStep] && nextStep !in visited) {
                        steps.compute(nextStep) { _, oldPath ->
                            val newScore = path.score + 1
                            if (oldPath == null || oldPath.score > newScore)
                                Path(newScore, path.positions + nextStep)
                            else
                                oldPath
                        }
                    }
                }
            }
            return null
        }
    }

}

