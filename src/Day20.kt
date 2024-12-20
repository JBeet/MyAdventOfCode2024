import utils.*

fun main() {
    val testInput = readInput("Day20_test")
    verify(10, Day20(testInput).part1(10))
    val input = readInput("Day20")
    println(Day20(input).part1(100))
    verify(285, Day20(testInput).part2(50))
    println(Day20(input).part2(100))
}

class Day20(val grid: CharGrid) {
    constructor(input: String) : this(CharGrid(input))

    fun part1(minSaving: Int): Int {
        val basePath = findBasePath()
        val shortcuts = basePath.positions.flatMapIndexed { fromIndex, fromPos ->
            Direction.entries.mapNotNull { direction ->
                val wallPos = fromPos + direction
                if (grid[wallPos] != '#') null
                else {
                    val toPos = wallPos + direction
                    val toIndex = basePath.positions.indexOf(toPos)
                    if (toIndex < fromIndex) // incl -1
                        null
                    else
                        wallPos to (toIndex - fromIndex - 2)
                }
            }
        }
        val savings = shortcuts.groupingBy { it.second }.eachCount()
        return savings.filter { it.key >= minSaving }.values.sum()
    }

    data class Cheat(val from: Position, val to: Position, val cheatDuration: Int, val savedTime: Int)

    fun part2(minSaving: Int): Int {
        val basePath = findBasePath()
        val maxCheat = 20
        val cheats = basePath.positions.flatMapIndexed { fromIndex, fromPos ->
            println("$fromIndex of " + basePath.positions.size)
            basePath.positions.mapIndexedNotNull { toIndex: Int, toPosition: Position ->
                when {
                    fromIndex + minSaving >= toIndex -> null
                    fromPos.manhattanDistanceTo(toPosition) > maxCheat -> null
                    else -> {
                        val path =
                            bestWallPath(fromPos, toPosition, maxCheat.coerceAtMost(toIndex - fromIndex - minSaving))
                        val savedTime = toIndex - fromIndex - path
                        if (savedTime >= minSaving)
                            Cheat(fromPos, toPosition, path, savedTime)
                        else
                            null
                    }
                }
            }
        }
//        println(cheats)
        val savings = cheats.groupingBy { it.savedTime }.eachCount().toSortedMap()
        savings.forEach { (u, t) -> println("There are $t cheats that save $u picoseconds") }
        return savings.values.sum()
    }

    private fun bestWallPath(fromPos: Position, toPosition: Position, maxCheat: Int) =
        Direction.entries.fold(maxCheat + 1) { maxLength, dir ->
            findPath(fromPos + dir, toPosition) { pos, score ->
                pos in grid && score < maxLength
            }.let { if (it == null) maxLength else maxLength.coerceAtMost(it.score + 1) }
        }

    data class Path(val score: Int, val positions: List<Position>)

    private fun findBasePath(): Path =
        checkNotNull(findPath(grid.find('S'), grid.find('E')) { pos, _ -> grid[pos] != '#' }) { "Path not found" }

    private fun findPath(start: Position, endPoint: Position, isAllowed: (Position, Int) -> Boolean): Path? {
        val steps = mutableMapOf(start to Path(0, listOf(start)))
        val visited = mutableSetOf<Position>()
        while (steps.isNotEmpty()) {
            val (step, path) = steps.minBy { it.value.score }
            if (step == endPoint) return path
            if (step !in grid) continue
            visited.add(step)
            steps.remove(step)
            Direction.entries.forEach { dir ->
                val nextStep = step + dir
                if (isAllowed(nextStep, path.score) && nextStep !in visited) {
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
