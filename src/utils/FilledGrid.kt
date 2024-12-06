package utils

fun filledCharCells(input: List<String>) = input.map { it.toList() }

open class FilledCharGrid(cells: List<List<Char>>, empty: Char = '.') : FilledGrid<Char>(cells, empty) {
    constructor(input: List<String>) : this(filledCharCells(input))

    override fun transpose() = FilledCharGrid(cells.transpose(), empty)
    override fun rotate90() = FilledCharGrid(rotateCells90(), empty)
}

fun <C> toFilled(src: Map<Position, C>, empty: C, width: Int, height: Int): List<List<C>> =
    List(height) { r -> List(width) { c -> src[Position(r, c)] ?: empty } }

open class FilledGrid<C>(
    protected val cells: List<List<C>>,
    protected val empty: C,
    final override val bounds: ZeroBasedBounds = ZeroBasedBounds(cells.size, cells[0].size)
) : AbstractGrid<C>(bounds) {
    constructor(cells: Map<Position, C>, empty: C, width: Int, height: Int) :
            this(toFilled(cells, empty, width, height), empty)

    val height = bounds.height
    val width = bounds.width

    init {
        check(cells.all { it.size.toLong() == width }) { "expected length $width" }
    }

    val allPositions
        get() = bounds.rowRange.flatMapTo(mutableSetOf()) { r ->
            bounds.columnRange.map { c -> Position(r, c) }
        }
    val rows get() = bounds.rowRange.asSequence().map { row(it) }
    val columns get() = bounds.columnRange.asSequence().map { column(it) }
    val nonEmptyCells
        get() = cells.asSequence().flatMapIndexed { rowIndex, row ->
            row.asSequence().mapIndexedNotNull { colIndex, c ->
                if (c == empty) null else (Position(rowIndex, colIndex) to c)
            }
        }

    override fun row(r: Long): GridLine<C> =
        if (bounds.hasRow(r)) GridList(r, cells[r.toInt()], empty) else EmptyLine(r, empty)

    override fun column(c: Long) = if (bounds.hasColumn(c)) ColumnGridLine(c.toInt()) else EmptyLine(c, empty)

    override fun cell(p: Position) = cell(p.row, p.column)
    override fun cell(r: Int, c: Int) = if (r in bounds.rowRange && c in bounds.columnRange) cells[r][c] else empty
    override fun cell(r: Long, c: Long) = cell(r.toInt(), c.toInt())

    private inner class ColumnGridLine(private val columnIndex: Int) : GridLine<C> {
        private val size: Int = height.toInt()
        private val rowRange = 0..<size
        override val index: Long = columnIndex.toLong()
        override val isEmpty: Boolean by lazy { rowRange.all { cell(it) == empty } }
        override fun cell(idx: Long): C = cell(idx.toInt())
        private fun cell(idx: Int): C = if (idx in rowRange) cells[idx][columnIndex] else empty
        override fun findAll(predicate: (C) -> Boolean): Map<Long, C> = rowRange.mapNotNull { row ->
            val cell = cells[row][columnIndex]
            if (predicate(cell)) (row.toLong() to cell) else null
        }.toMap()
    }

    override fun findAll(predicate: (C) -> Boolean): Map<Position, C> = buildMap {
        cells.forEachIndexed { rowIndex, rowData ->
            rowData.forEachIndexed { colIndex, cell ->
                if (predicate(cell)) put(Position(rowIndex, colIndex), cell)
            }
        }
    }

    override fun forEachNonEmpty(action: (Position) -> Unit) = forEachWithEmpty { if (cell(it) != empty) action(it) }
    override fun countNonEmpty(predicate: (Position) -> Boolean) = countWithEmpty { cell(it) != empty && predicate(it) }

    override fun transpose() = FilledGrid(cells.transpose(), empty)
    open fun rotate90() = FilledGrid(rotateCells90(), empty, bounds.swap())
    fun rotateCells90() = columns.mapTo(mutableListOf()) { it.toList(height).reversed() }

    open fun bgColor(pos: Position): AnsiColor = AnsiColor.DEFAULT
    open fun fgColor(pos: Position): AnsiColor = AnsiColor.DEFAULT
    override fun toString() = buildString {
        bounds.rowRange.forEach { rowIndex ->
            bounds.columnRange.forEach { colIndex ->
                val pos = Position(rowIndex, colIndex)
                val fgColor = fgColor(pos)
                val bgColor = bgColor(pos)
                if (fgColor != AnsiColor.DEFAULT) append(fgColor.fgCode())
                if (bgColor != AnsiColor.DEFAULT) append(bgColor.bgCode())
                append(cellAsString(pos, cell(pos)))
                if (fgColor != AnsiColor.DEFAULT) append(AnsiColor.DEFAULT.fgCode())
                if (bgColor != AnsiColor.DEFAULT) append(AnsiColor.DEFAULT.bgCode())
            }
            append('\n')
        }
    }
}

private data class GridList<C>(override val index: Long, private val cells: List<C>, private val empty: C) :
    GridLine<C> {
    private val size: Int = cells.size
    private val columnRange = 0..<size
    override val isEmpty: Boolean get() = cells.all { it == empty }
    override fun cell(idx: Long): C = cell(idx.toInt())
    fun cell(idx: Int): C = if (idx in columnRange) cells[idx] else empty

    override fun findAll(predicate: (C) -> Boolean): Map<Long, C> = cells.mapIndexedNotNull { idx, cell ->
        if (predicate(cell)) (idx.toLong() to cell) else null
    }.toMap()

    override fun toString(): String = cells.joinToString("")
}
