package utils

interface Bounds {
    val rowRange: LongRange
    val columnRange: LongRange
    fun hasRow(r: Long): Boolean
    fun hasColumn(c: Long): Boolean
    operator fun contains(p: Position): Boolean = hasRow(p.row) && hasColumn(p.column)
}

class DetectBounds(positions: Iterable<Position>) : Bounds {
    private val rowIndices = positions.map { it.row }.toSortedSet()
    private val colIndices = positions.map { it.column }.toSortedSet()
    private val minRow = rowIndices.first()
    private val maxRow = rowIndices.last()
    private val minColumn = colIndices.first()
    private val maxColumn = colIndices.last()
    override val rowRange: LongRange = minRow..maxRow
    override val columnRange: LongRange = minColumn..maxColumn
    override fun hasRow(r: Long) = r in rowIndices
    override fun hasColumn(c: Long) = c in colIndices
}

data class ZeroBasedBounds(val height: Long, val width: Long) : Bounds {
    constructor(height: Int, width: Int) : this(height.toLong(), width.toLong())

    override val rowRange: LongRange = (0..<height)
    override val columnRange: LongRange = (0..<width)
    override fun hasRow(r: Long): Boolean = r in rowRange
    override fun hasColumn(c: Long): Boolean = c in columnRange
    fun swap(): ZeroBasedBounds = ZeroBasedBounds(width, height)
}
