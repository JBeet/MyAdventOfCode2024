import kotlin.math.absoluteValue

fun main() {
    val testInput = readInput("Day02_test")
    check(part1(testInput) == 2) { "Expected 2 but was " + part1(testInput) }
    val input = readInput("Day02")
    println(part1(input))
    check(part2(testInput) == 4) { "Expected 4 but was " + part2(testInput) }
    println(part2(input))
}

fun part1(input: String): Int = input.intLists().count { it.isSafe() }
fun part2(input: String) =
    input.intLists().count { original -> original.isSafe() || dampened(original).any { it.isSafe() } }
fun dampened(list: List<Int>): List<List<Int>> = list.indices.map { list.toMutableList().apply { removeAt(it) } }
private fun List<Int>.isSafe() =
    this.hasValidOrder() && zipWithNext { a, b -> (a - b).absoluteValue }.all { it in 1..3 }
private fun List<Int>.hasValidOrder() = this == sorted() || this == sortedDescending()
