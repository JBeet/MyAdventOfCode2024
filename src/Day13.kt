import utils.*

fun main() {
    val testInput = readInput("Day13_test")
    verify(480, Day13.part1(testInput))
    val input = readInput("Day13")
    println(Day13.part1(input))
//    verify(1206, Day13.part2(testInput))
    println(Day13.part2(input))
}

object Day13 {
    fun Puzzle13(s: String): Puzzle13 {
        val (sA, sB, sP) = s.lines()
        val (aX, aY) = sA.longs()
        val (bX, bY) = sB.longs()
        val (pX, pY) = sP.longs()
        return Puzzle13(aX, aY, bX, bY, pX, pY)
    }

    fun parse(input: String) = input.paragraphs().map { Puzzle13(it) }
    fun part1(input: String) = parse(input).sumOf { it.minCost ?: 0 }
    fun part2(input: String) = parse(input).map { it.part2() }.sumOf { it.minCostNoLimit ?: 0 }
}

private fun Sequence<Long>.toRepeating(): Sequence<Long> = sequence {
    val iter = iterator()
    if (!iter.hasNext())
        return@sequence
    val first = iter.next()
    yield(first)
    if (!iter.hasNext())
        return@sequence
    var current = iter.next()
    val delta = current - first
    while (true) {
        yield(current)
        current += delta
    }
}

data class Puzzle13(val aX: Long, val aY: Long, val bX: Long, val bY: Long, val pX: Long, val pY: Long) {
    private val solutions = sequence {
        val lcmX = leastCommonMultiple(aX, bX)
        val lcmY = leastCommonMultiple(aY, bY)
        val xA =
            naturalNumberLongs.take(lcmX.toInt() * 2).filter { it: Long -> (pX - it * aX) % bX == 0L }.toRepeating()
        val xyA = xA.take(lcmY.toInt() * 2).filter { it: Long -> (pY - it * aY) % bY == 0L }.toRepeating()
        val valid = xyA.map { it to b(it) }
        val list = valid.take(2).toList()
        if (list.size < 2) {
            list.forEach { yield(it) }
        } else {
            val (first, second) = list
            val diffA = second.a - first.a
            val diffB = second.b - first.b
            val diffY = second.y - first.y
            val targetIndex = (pY - first.y) / diffY
            val a = first.a + (targetIndex * diffA)
            val b = first.b + (targetIndex * diffB)
            if (a >= 0 && b >= 0 && (a * aY + b * bY == pY))
                yield(a to b)
        }
    }

    private fun a(b: Long) = (pX - b * bX) / aX
    private fun b(a: Long) = (pX - a * aX) / bX
    private val Pair<Long, Long>.a get() = first
    private val Pair<Long, Long>.b get() = second
    private val Pair<Long, Long>.x get() = x(first, second)
    private val Pair<Long, Long>.y get() = y(first, second)
    private fun x(a: Long, b: Long) = a * aX + b * bX
    private fun y(a: Long, b: Long) = a * aY + b * bY

    val minCost: Long? get() = solutions.filter { (a, b) -> a <= 100 && b <= 100 }.minOfOrNull { (a, b) -> 3 * a + b }
    val minCostNoLimit get() = solutions.minOfOrNull { (a, b) -> 3 * a + b }
    fun part2() = copy(pX = pX + 10000000000000L, pY = pY + 10000000000000L)
}
