package utils

import java.math.BigInteger
import java.security.MessageDigest
import kotlin.io.path.Path
import kotlin.io.path.readText

/**
 * Reads lines from the given input txt file.
 */
fun readInput(name: String) = Path("src/$name.txt").readText().trim()

/**
 * Converts string to md5 hash.
 */
fun String.md5() = BigInteger(1, MessageDigest.getInstance("MD5").digest(toByteArray()))
    .toString(16)
    .padStart(32, '0')

fun String.nonEmptyLines(): List<String> = lines().filter { it.isNotBlank() }
fun String.findWithRegex(pattern: String) = Regex(pattern).findAll(this).map { it.value }
fun String.ints() = signedInts().also { list -> check(list.all { it > 0 }) }
fun String.intsOrZero() = signedInts().also { list -> check(list.all { it >= 0 }) }
fun String.signedInts(): List<Int> = bigIntegers().mapTo(mutableListOf()) { it.intValueExact() }
fun String.longs() = signedLongs().also { longs -> check(longs.all { it > 0 }) }
fun String.longsOrZero() = signedLongs().also { longs -> check(longs.all { it >= 0 }) }
fun String.signedLongs(): List<Long> = bigIntegers().mapTo(mutableListOf()) { it.longValueExact() }

fun String.bigIntegers() = findWithRegex("""-?\d+""").map { it.toBigInteger() }
fun String.paragraphs(): List<String> = split(Regex("\\R\\R"))
fun String.intLists(): List<List<Int>> = nonEmptyLines().map { it.ints() }
fun String.longLists() = nonEmptyLines().map { it.longs() }

@JvmName("transposeStrings")
fun List<String>.transpose(): List<String> = this[0].indices.map { this.column(it) }
fun List<String>.column(c: Int): String = buildString { this@column.forEach { s -> append(s[c]) } }

@JvmName("transposeLists")
fun <T> List<List<T>>.transpose(): List<List<T>> = this[0].indices.map { this.column(it) }
fun <T> List<List<T>>.column(c: Int): List<T> = map { it[c] }

fun <E> List<E>.sublistBefore(item: E, missingDelimiterValue: List<E> = this): List<E> {
    val pos = indexOf(item)
    return if (pos < 0) missingDelimiterValue else subList(0, pos)
}

fun <E> List<E>.sublistAfter(item: E, missingDelimiterValue: List<E> = this): List<E> {
    val pos = indexOf(item)
    return if (pos < 0) missingDelimiterValue else subList(pos + 1, size)
}

fun <E> MutableList<E>.swapItem(itemA: E, itemB: E) = swapIndex(indexOf(itemA), indexOf(itemB))
fun <E> MutableList<E>.swapIndex(indexA: Int, indexB: Int) {
    val temp = this[indexA]
    this[indexA] = this[indexB]
    this[indexB] = temp
}

fun Int.concat(o: Int) = (toString() + o.toString()).toInt()
fun Long.concat(o: Long) = (toString() + o.toString()).toLong()

val naturalNumberInts: Sequence<Int> = generateSequence(0) { it + 1 }
val naturalNumberLongs: Sequence<Long> = generateSequence(0L) { it + 1L }
val positiveIntegers: Sequence<Int> = generateSequence(1) { it + 1 }
val positiveLongs: Sequence<Long> = generateSequence(1L) { it + 1L }

fun Sequence<Long>.toRepeating(): Sequence<Long> = sequence {
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

fun leastCommonMultiple(a: Long, b: Long): Long = (a * b) / greatestCommonDivisor(a, b)
tailrec fun greatestCommonDivisor(a: Long, b: Long): Long {
    check(b > 0L) { "No GCD for $a, $b" }
    val mod = a % b
    return if (mod == 0L) b else greatestCommonDivisor(b, mod)
}

fun <E> List<E>.combinations(): Sequence<List<E>> =
    if (isEmpty())
        sequenceOf(emptyList())
    else {
        val (item, remainder) = headToTail()
        remainder.combinations() + remainder.combinations().map { listOf(item) + it }
    }

fun <E> List<E>.combinationsOfSize(size: Int): Sequence<List<E>> = generateCombinationsOfSize(size, emptyList())
private fun <E> List<E>.generateCombinationsOfSize(size: Int, prefix: List<E>): Sequence<List<E>> =
    if (size == 0)
        sequenceOf(prefix)
    else if (size > this.size)
        emptySequence()
    else if (size == this.size)
        sequenceOf(prefix + this)
    else {
        val (item, remainder) = headToTail()
        remainder.generateCombinationsOfSize(size, prefix) +
                remainder.generateCombinationsOfSize(size - 1, prefix + item)
    }

fun <E> List<E>.headToTail(): Pair<E, List<E>> {
    val head = get(0)
    val tail = subList(1, size)
    return head to tail
}

fun <E> Collection<E>.permutations(): Sequence<List<E>> = toMutableList().generatePermutations(size - 1)

private fun <E> MutableList<E>.generatePermutations(k: Int): Sequence<List<E>> = when {
    k == 0 -> sequenceOf(toList())
    k % 2 != 0 -> sequence {
        yieldAll(generatePermutations(k - 1))
        repeat(k) {
            swapIndex(it, k)
            yieldAll(generatePermutations(k - 1))
        }
    }

    else -> sequence {
        yieldAll(generatePermutations(k - 1))
        repeat(k) {
            swapIndex(0, k)
            yieldAll(generatePermutations(k - 1))
        }
    }
}

fun verify(expected: Long, actual: Int) {
    check(actual.toLong() == expected) { "Expected $expected but was $actual" }
}

fun verify(expected: Long, actual: Long) {
    check(actual == expected) { "Expected $expected but was $actual" }
}

enum class AnsiColor(private val fg: Int, private val bg: Int) {
    BLACK(30, 40), RED(31, 41), GREEN(32, 42), YELLOW(33, 43), BLUE(34, 44),
    MAGENTA(35, 45), CYAN(36, 46), WHITE(37, 47),
    BRIGHT_BLACK(90, 100), BRIGHT_RED(91, 101), BRIGHT_GREEN(92, 102), BRIGHT_YELLOW(93, 103), BRIGHT_BLUE(94, 104),
    BRIGHT_MAGENTA(95, 105), BRIGHT_CYAN(96, 106), BRIGHT_WHITE(97, 107),

    DEFAULT(39, 49), RESET(0, 0);

    fun fgCode(): String = "\u001b[${fg}m"
    fun bgCode(): String = "\u001b[${bg}m"
}
