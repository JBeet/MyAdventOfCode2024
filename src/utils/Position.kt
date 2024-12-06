package utils

import kotlin.math.abs

data class Position(val row: Long, val column: Long) {
    constructor(row: Int, column: Int) : this(row.toLong(), column.toLong())

    val rowBefore: Sequence<Position> = (0..<column).asSequence().map { Position(row, it) }
    val columnBefore: Sequence<Position> = (0..<row).asSequence().map { Position(it, column) }

    operator fun plus(term: Position) = Position(row + term.row, column + term.column)
    operator fun plus(term: Delta): Position = this + term.delta
    operator fun minus(term: Position) = Position(row - term.row, column - term.column)
    operator fun minus(term: Delta): Position = this - term.delta
    operator fun unaryMinus() = Position(-row, -column)
    operator fun times(f: Int) = times(f.toLong())
    operator fun times(f: Long) = Position(row * f, column * f)
    fun manhattanDistanceTo(o: Position) = abs(row - o.row) + abs(column - o.column)
    fun transpose(): Position = Position(column, row)
    fun rotate90(height: Long) = Position(column, height - 1 - row)
    fun rotate180(height: Long, width: Long) = Position(height - 1 - row, width - 1 - column)
    fun rotate270(width: Long) = Position(width - 1 - column, row)

    override fun toString(): String = "($row,$column)"

    companion object {
        val zero: Position = Position(0L, 0L)
    }
}

operator fun Int.times(f: Position) = f * toLong()
operator fun Long.times(f: Position) = f * this

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
