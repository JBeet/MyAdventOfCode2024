import utils.paragraphs
import utils.readInput
import utils.verify

fun main() {
    val testInput = readInput("Day19_test")
    verify(6, Day19(testInput).part1())
    println("Test input OK")
    val input = readInput("Day19")
    println(Day19(input).part1())
    verify(16, Day19(testInput).part2())
    println(Day19(input).part2())
}

data class Day19(val towelTypes: List<String>, val designs: List<String>) {
    private constructor(st: String, sd: String) : this(st.split(",").map { it.trim() }, sd.lines())
    private constructor(input: List<String>) : this(input[0], input[1])
    constructor(input: String) : this(input.paragraphs())

    fun part1(): Int = designs.count { hasArrangement(it, mutableMapOf("" to true)) }

    private fun hasArrangement(design: String, memory: MutableMap<String, Boolean>): Boolean =
        memory.getOrPut(design) {
            towelTypes.filter { design.startsWith(it) }.any { hasArrangement(design.drop(it.length), memory) }
        }

    fun part2() = designs.sumOf { countArrangements(it, mutableMapOf("" to 1L)) }

    private fun countArrangements(design: String, memory: MutableMap<String, Long>): Long =
        memory.getOrPut(design) {
            towelTypes.filter { design.startsWith(it) }.sumOf { countArrangements(design.drop(it.length), memory) }
        }
}
