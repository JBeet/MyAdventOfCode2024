package utils

abstract class AbstractGrid<C>(protected open val bounds: Bounds) : Grid<C> {
    fun connections(pos: Position) = connections(pos, cell(pos))
    open fun connections(pos: Position, cell: C): Iterable<Position> = deltas(pos, cell).map { pos + it.delta }
    open fun deltas(pos: Position, cell: C): Collection<Delta> =
        if (cell is GridCell) cell.directions else emptySet()

    interface GridTraverse<R> {
        val initialPosition: Position
        val initialValue: R
        fun shouldUpdate(pos: Position, oldValue: R, curValue: R): Boolean
        fun calculateNextValue(pos: Position, oldValue: R): R
        fun nextPositions(pos: Position, curValue: R): Iterable<Position>
    }

    fun <R> traverse(spec: GridTraverse<R>): Map<Position, R> {
        val target = mutableMapOf<Position, R>()
        val traverse = DeepRecursiveFunction<Pair<Position, R>, Unit> { (pos, acc) ->
            val known = target[pos]
            if (known == null || spec.shouldUpdate(pos, known, acc)) {
                target[pos] = acc
                val result = spec.calculateNextValue(pos, acc)
                spec.nextPositions(pos, acc).forEach { nextPosition ->
                    callRecursive(nextPosition to result)
                }
            }
        }
        traverse(spec.initialPosition to spec.initialValue)
        return target
    }

    abstract inner class TraverseNESW<R>(override val initialPosition: Position, override val initialValue: R) :
        GridTraverse<R> {
        override fun nextPositions(pos: Position, curValue: R): Iterable<Position> =
            listOf(Direction.N, Direction.E, Direction.S, Direction.W).map { pos + it.delta }
                .filter { it in this@AbstractGrid }
    }

    open operator fun contains(pos: Position) = pos in bounds

    abstract inner class TraverseConnections<R>(override val initialPosition: Position, override val initialValue: R) :
        GridTraverse<R> {
        override fun nextPositions(pos: Position, curValue: R): Iterable<Position> = connections(pos)
    }

    fun <R> traverseConnections(
        pos: Position, value: R, shouldUpdate: (R, R) -> Boolean, calculateNextValue: (Position, R) -> R
    ) = traverse(object : TraverseConnections<R>(pos, value) {
        override fun shouldUpdate(pos: Position, oldValue: R, curValue: R): Boolean = shouldUpdate(oldValue, curValue)
        override fun calculateNextValue(pos: Position, oldValue: R): R = calculateNextValue(pos, oldValue)
    })

    fun <R : Any> traverseConnections(pos: Position, value: R, cellHandler: (Position, R) -> R?) =
        traverse(object : TraverseConnections<R?>(pos, value) {
            override fun shouldUpdate(pos: Position, oldValue: R?, curValue: R?) = curValue != null
            override fun calculateNextValue(pos: Position, oldValue: R?): R? =
                if (oldValue == null) null else cellHandler(pos, oldValue)
        })

    fun forEachWithEmpty(action: (Position) -> Unit) = bounds.rowRange.forEach { rowIndex ->
        bounds.columnRange.forEach { colIndex -> action(Position(rowIndex, colIndex)) }
    }

    fun countWithEmpty(predicate: (Position) -> Boolean) = bounds.rowRange.sumOf { rowIndex ->
        bounds.columnRange.count { colIndex -> predicate(Position(rowIndex, colIndex)) }
    }

    open fun cellAsString(pos: Position, cell: C): String = when (connections(pos, cell).toSet()) {
        emptySet<Direction>() -> cell.toString()
        else -> charFor(deltas(pos, cell).toSet())?.toString() ?: cell.toString()
    }

    private fun charFor(directions: Set<Delta>): Char? = when (directions) {
        emptySet<Direction>() -> ' '
        setOf(Direction.N) -> '╵'
        setOf(Direction.E) -> '╶'
        setOf(Direction.S) -> '╷'
        setOf(Direction.W) -> '╴'
        setOf(Direction.N, Direction.E) -> '└'
        setOf(Direction.N, Direction.S) -> '│'
        setOf(Direction.N, Direction.W) -> '┘'
        setOf(Direction.E, Direction.S) -> '┌'
        setOf(Direction.E, Direction.W) -> '─'
        setOf(Direction.S, Direction.W) -> '┐'
        setOf(Direction.N, Direction.E, Direction.S) -> '├'
        setOf(Direction.N, Direction.E, Direction.W) -> '┴'
        setOf(Direction.N, Direction.S, Direction.W) -> '┤'
        setOf(Direction.E, Direction.S, Direction.W) -> '┬'
        setOf(Direction.N, Direction.E, Direction.S, Direction.W) -> '┼'
        else -> null
    }
}
