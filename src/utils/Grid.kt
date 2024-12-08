package utils

interface Grid<C> {
    operator fun get(p: Position) = cell(p)
    fun cell(p: Position): C
    fun cell(r: Int, c: Int): C
    fun row(r: Int): GridLine<C>
    fun column(c: Int): GridLine<C>
    fun forEachNonEmpty(action: (Position) -> Unit)
    fun countNonEmpty(predicate: (Position) -> Boolean): Int
    fun findAll(value: C): Set<Position> = findAll { cell -> cell == value }.keys
    fun findAll(predicate: (C) -> Boolean): Map<Position, C>
    fun find(value: C): Position = findAll(value).single()
    fun transpose(): Grid<C>
    operator fun contains(pos: Position): Boolean
}

interface GridCell {
    val directions: Set<Delta> get() = emptySet()
}

interface GridLine<C> {
    val index: Int
    val isEmpty: Boolean
    fun cell(idx: Int): C
    operator fun get(idx: Int): C = cell(idx)
    fun toList(size: Int) = (0..<size).map { cell(it) }
    fun findAll(value: C): Set<Int> = findAll { cell -> cell == value }.keys
    fun findAll(predicate: (C) -> Boolean): Map<Int, C>
}

data class EmptyLine<C>(override val index: Int, private val empty: C) : GridLine<C> {
    override val isEmpty: Boolean get() = true
    override fun cell(idx: Int): C = empty
    override fun findAll(predicate: (C) -> Boolean): Map<Int, C> = emptyMap()
}
