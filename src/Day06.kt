import utils.*

fun main() {
    val testInput = readInput("Day06_test")
    verify(41, Day06(testInput).part1())
    val input = readInput("Day06")
    println(Day06(input).part1())
    verify(6, Day06(testInput).part2())
    println(Day06(input).part2())
}

class Day06(private val input: String) {
    fun part1(): Int {
        val directionMap = findPath(FilledCharGrid(input.lines()))
        return directionMap.count { it.value.isNotEmpty() }
    }

    private fun findPath(grid: FilledCharGrid): Map<Position, Set<Direction>> {
        val map = mutableMapOf<Position, Set<Direction>>()
        var pos = grid.find('^')
        var direction = Direction.N
        while (pos in grid) {
            val known = map[pos] ?: emptySet()
            if (direction !in known) {
                map[pos] = known + direction
                while (grid[pos + direction] == '#') {
                    direction = direction.rotateCW
                }
                pos += direction
            }
        }
        return map
    }

    private fun hasCycle(grid: FilledCharGrid): Boolean {
        val map = mutableMapOf<Position, Set<Direction>>()
        var pos = grid.find('^')
        var direction = Direction.N
        while (pos in grid) {
            val known = map[pos] ?: emptySet()
            if (direction in known) return true
            map[pos] = known + direction
            while (grid[pos + direction] == '#') {
                direction = direction.rotateCW
            }
            pos += direction
        }
        return false
    }

    fun part2(): Int = input.indices.filter { input[it] == '.' }.count { idx ->
        val adjusted = StringBuilder(input).apply { this[idx] = '#' }.toString()
        hasCycle(FilledCharGrid(adjusted.lines()))
    }
}

