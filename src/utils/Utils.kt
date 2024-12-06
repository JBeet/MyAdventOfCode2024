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
fun String.ints() = findWithRegex("""-?\d+""").map { it.toInt() }.toList()
fun String.longs() = findWithRegex("""-?\d+""").map { it.toLong() }.toList()
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
