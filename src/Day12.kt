import utils.*

fun main() {
    val testInput = readInput("Day12_test")
    verify(1930, Day12(testInput).part1())
    val input = readInput("Day12")
    println(Day12(input).part1())
    verify(1206, Day12(testInput).part2())
    println(Day12(input).part2())
}

class Day12(val grid: CharGrid) {
    constructor(input: String) : this(CharGrid(input))

    private val regions = mutableListOf<Region>()

    init {
        enumerateRegions()
    }

    fun part1(): Int = regions.sumOf { it.price }
    fun part2(): Int = regions.sumOf { it.discountedPrice }

    private fun enumerateRegions() {
        grid.nonEmptyCells.forEach { (p, ch) ->
            if (regions.none { p in it })
                regions.add(Region(ch, grid.floodFill(p).sorted()))
        }
    }

    override fun toString(): String = grid.toString()
}

data class Side(val side: Direction, val positions: Set<Position>)

class Region(private val ch: Char, private val items: Collection<Position>) {
    private val area: Int get() = items.size
    private val perimeter: Int get() = items.sumOf { p -> 4 - p.neighbours4.count { n -> n in items } }
    val price get() = area * perimeter

    private val sides = mutableListOf<Side>()

    init {
        enumerateSides()
    }

    private val nrOfSides: Int = sides.size
    val discountedPrice get() = area * nrOfSides

    private fun enumerateSides() {
        items.forEach { pos ->
            Direction.entries.filter { dir -> pos.isPartOfSide(dir) && !sideVisited(dir, pos) }.forEach { dir ->
                val positions = positionsFrom(pos, dir.rotateCW).takeWhile { it.isPartOfSide(dir) } +
                        positionsFrom(pos, dir.rotateCCW).takeWhile { it.isPartOfSide(dir) }
                sides.add(Side(dir, positions.toSet()))
            }
        }
    }

    private fun Position.isPartOfSide(dir: Direction) = this in items && (this + dir) !in items

    private fun sideVisited(d: Direction, p: Position) = sides.any { it.side == d && p in it.positions }
    operator fun contains(p: Position): Boolean = p in items

    override fun toString(): String = "Region[$ch] $items = $perimeter / $nrOfSides"
}
