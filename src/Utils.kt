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
