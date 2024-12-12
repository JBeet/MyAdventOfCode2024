package utils

fun main() {
    val combinations = listOf(1, 3, 6).combinations().toSet()
    assertEquals(
        setOf(emptyList(), listOf(1), listOf(3), listOf(6), listOf(1, 3), listOf(1, 6), listOf(3, 6), listOf(1, 3, 6)),
        combinations
    )
    val combinationsOfSize3 = listOf(1, 2, 3, 4).combinationsOfSize(3).toSet()
    assertEquals(setOf(listOf(1, 2, 3), listOf(1, 2, 4), listOf(1, 3, 4), listOf(2, 3, 4)), combinationsOfSize3)
    val permutations = listOf(2, 4, 7).permutations().toSet()
    assertEquals(
        setOf(listOf(2, 4, 7), listOf(2, 7, 4), listOf(4, 2, 7), listOf(4, 7, 2), listOf(7, 2, 4), listOf(7, 4, 2)),
        permutations
    )
    assertEquals((0..10).toSet(), naturalNumberInts.takeWhile { it <= 10 }.toSet())
    assertEquals((3L..7L).toSet(), naturalNumberLongs.drop(3).takeWhile { it <= 7 }.toSet())
    assertEquals((1..10).toSet(), positiveIntegers.takeWhile { it <= 10 }.toSet())
    assertEquals((1L..7L).toSet(), positiveLongs.takeWhile { it <= 7 }.toSet())

    val gridText = """o...
        |oo..
        |..o.
        |.x.o
        |...o
    """.trimMargin()
    val grid = CharGrid(gridText)
    assertEquals(Position(3, 1), grid.find('x'))
    assertEquals(
        setOf(Position(0, 0), Position(1, 0), Position(1, 1), Position(2, 2), Position(3, 3), Position(4, 3)),
        grid.findAll('o')
    )
    val positions =
        setOf(
            Position(0, 0),
            Position(1, 0),
            Position(1, 1),
            Position(3, 2),
            Position(2, 3),
            Position(3, 3),
            Position(4, 4)
        )
    assertEquals(
        setOf(
            setOf(Position(0, 0), Position(1, 0), Position(1, 1)),
            setOf(Position(3, 2), Position(2, 3), Position(3, 3)),
            setOf(Position(4, 4))
        ),
        positions.areas()
    )

}

fun <E> assertEquals(expected: E, actual: E) {
    if (expected == actual) return
    error("Expected $expected but was $actual")
}
