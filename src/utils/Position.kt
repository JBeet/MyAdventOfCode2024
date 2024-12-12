package utils

import kotlin.math.abs

data class Position(val row: Int, val column: Int) : Comparable<Position> {
    val rowBefore: Sequence<Position> = (0..<column).asSequence().map { Position(row, it) }
    val columnBefore: Sequence<Position> = (0..<row).asSequence().map { Position(it, column) }

    operator fun plus(term: Position) = Position(row + term.row, column + term.column)
    operator fun plus(term: Delta): Position = this + term.delta
    operator fun minus(term: Position) = Position(row - term.row, column - term.column)
    operator fun minus(term: Delta): Position = this - term.delta
    operator fun unaryMinus() = Position(-row, -column)
    operator fun times(f: Int) = Position(row * f, column * f)
    fun manhattanDistanceTo(o: Position) = abs(row - o.row) + abs(column - o.column)
    fun transpose(): Position = Position(column, row)
    fun rotate90(height: Int) = Position(column, height - 1 - row)
    fun rotate180(height: Int, width: Int) = Position(height - 1 - row, width - 1 - column)
    fun rotate270(width: Int) = Position(width - 1 - column, row)

    val neighbours4 get() = Direction.entries.map { this + it }

    override fun compareTo(other: Position): Int {
        val cr = row.compareTo(other.row)
        return if (cr == 0) column.compareTo(other.column) else cr
    }

    override fun toString(): String = "($row,$column)"

    companion object {
        val zero: Position = Position(0, 0)
    }
}

operator fun Int.times(f: Position) = f * this

interface Delta {
    val delta: Position
    operator fun unaryMinus(): Delta
}

enum class Direction(override val delta: Position) : Delta {
    N(Position(-1, 0)), E(Position(0, +1)), S(Position(+1, 0)), W(Position(0, -1));

    override fun unaryMinus(): Direction = when (this) {
        N -> S
        E -> W
        S -> N
        W -> E
    }

    val rotateCW: Direction
        get() = when (this) {
            N -> E
            E -> S
            S -> W
            W -> N
        }
    val rotateCCW: Direction get() = -rotateCW
}

val directions = Direction.entries

fun positionsFrom(position: Position, direction: Direction) =
    naturalNumberInts.map { position + (direction.delta * it) }

typealias Directions = Set<Direction>

fun directions(s: String): Directions = s.mapTo(mutableSetOf()) { direction(it) }
fun direction(ch: Char) = Direction.valueOf(ch.toString())

enum class Direction8(override val delta: Position) : Delta {
    N(Position(-1, 0)), E(Position(0, +1)), S(Position(+1, 0)), W(Position(0, -1)),
    NE(Position(-1, +1)), SE(Position(+1, +1)), NW(Position(-1, -1)), SW(Position(+1, -1));

    override fun unaryMinus(): Direction8 = when (this) {
        N -> S
        E -> W
        S -> N
        W -> E
        NE -> SW
        SE -> NW
        NW -> SE
        SW -> NE
    }
}


fun Iterable<Position>.continuousAreas(): Set<Set<Position>> = buildAreas(this.toMutableSet())

private fun buildAreas(source: MutableSet<Position>) = buildSet<Set<Position>> {
    while (source.isNotEmpty()) {
        add(buildSet {
            val queue = mutableListOf(source.removeFirst())
            while (queue.isNotEmpty()) {
                val element = queue.removeAt(0)
                add(element)
                element.neighbours4.forEach {
                    if (source.remove(it))
                        queue.add(it)
                }
            }
        })
    }
}

fun <E> MutableSet<E>.removeFirst(): E = iterator().removeNext()
fun <E> MutableIterator<E>.removeNext(): E = next().also { remove() }
