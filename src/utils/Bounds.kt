package utils

interface Bounds {
    val rowRange: IntRange
    val columnRange: IntRange
    fun hasRow(r: Int): Boolean
    fun hasColumn(c: Int): Boolean
    operator fun contains(p: Position): Boolean = hasRow(p.row) && hasColumn(p.column)
}

class DetectBounds(positions: Iterable<Position>) : Bounds {
    private val rowIndices = positions.map { it.row }.toSortedSet()
    private val colIndices = positions.map { it.column }.toSortedSet()
    private val minRow = rowIndices.first()
    private val maxRow = rowIndices.last()
    private val minColumn = colIndices.first()
    private val maxColumn = colIndices.last()
    override val rowRange: IntRange = minRow..maxRow
    override val columnRange: IntRange = minColumn..maxColumn
    override fun hasRow(r: Int) = r in rowIndices
    override fun hasColumn(c: Int) = c in colIndices
}

data class ZeroBasedBounds(val height: Int, val width: Int) : Bounds {
    override val rowRange: IntRange = (0..<height)
    override val columnRange: IntRange = (0..<width)
    override fun hasRow(r: Int): Boolean = r in rowRange
    override fun hasColumn(c: Int): Boolean = c in columnRange
    fun swap(): ZeroBasedBounds = ZeroBasedBounds(width, height)
}
