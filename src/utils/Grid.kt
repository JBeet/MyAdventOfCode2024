package utils

interface Grid<C> {
    operator fun get(p: Position) = cell(p)
    fun cell(p: Position): C
    fun cell(r: Int, c: Int): C
    fun cell(r: Long, c: Long): C
    fun row(r: Long): GridLine<C>
    fun column(c: Long): GridLine<C>
    fun forEachNonEmpty(action: (Position) -> Unit)
    fun countNonEmpty(predicate: (Position) -> Boolean): Int
    fun findAll(value: C): Set<Position> = findAll { cell -> cell == value }.keys
    fun findAll(predicate: (C) -> Boolean): Map<Position, C>
    fun find(value: C): Position = findAll(value).single()
    fun transpose(): Grid<C>
}

interface GridCell {
    val directions: Set<Delta> get() = emptySet()
}

interface GridLine<C> {
    val index: Long
    val isEmpty: Boolean
    fun cell(idx: Long): C
    operator fun get(idx: Long): C = cell(idx)
    fun toList(size: Long) = (0..<size).map { cell(it) }
    fun findAll(value: C): Set<Long> = findAll { cell -> cell == value }.keys
    fun findAll(predicate: (C) -> Boolean): Map<Long, C>
}

data class EmptyLine<C>(override val index: Long, private val empty: C) : GridLine<C> {
    override val isEmpty: Boolean get() = true
    override fun cell(idx: Long): C = empty
    override fun findAll(predicate: (C) -> Boolean): Map<Long, C> = emptyMap()
}
