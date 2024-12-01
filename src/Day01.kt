import kotlin.math.absoluteValue

fun main() {
    fun part1(input: List<String>): Int {
        val lines = linesAsIntArrays(input)
        val nrs1 = lines.map { it[0] }.sorted()
        val nrs2 = lines.map { it[1] }.sorted()
        return nrs1.zip(nrs2).sumOf { (a, b) -> (a - b).absoluteValue }
    }

    fun part2(input: List<String>): Long {
        val lines = linesAsIntArrays(input)
        val nrs1 = lines.map { it[0] }
        val nrs2 = lines.map { it[1] }.groupingBy { it }.eachCount()
        return nrs1.sumOf { it.toLong() * (nrs2[it] ?: 0) }
    }

    // Or read a large test input from the `src/Day01_test.txt` file:
    val testInput = readInput("Day01_test")
    check(part1(testInput) == 11)

    // Read the input from the `src/Day01.txt` file.
    val input = readInput("Day01")
    part1(input).println()
    check(part2(testInput) == 31L)
    part2(input).println()
}
