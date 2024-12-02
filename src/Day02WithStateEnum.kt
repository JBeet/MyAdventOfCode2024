import kotlin.math.abs

fun main() {
    val testInput = readInput("Day02_test")
    check(Day02WithStateEnum.part1(testInput) == 2) { "Expected 2 but was " + Day02WithStateEnum.part1(testInput) }
    check(Day02WithStateEnum.part2(testInput) == 4) { "Expected 4 but was " + Day02WithStateEnum.part2(testInput) }

    val input = readInput("Day02")
    Day02WithStateEnum.showStates(input)
    println()
    println("Solution Part 1: " + Day02WithStateEnum.part1(input))
    println("Solution Part 2: " + Day02WithStateEnum.part2(input))
}

object Day02WithStateEnum {
    fun part1(input: String): Int =
        input.intLists().count { list -> list.allStates().all { it.isValidPart1 } }

    fun part2(input: String) =
        input.intLists().count { list -> list.allStates().none { it == State.Invalid } }

    fun showStates(input: String) {
        input.intLists().forEach { list ->
            print(stateColor(State.First).fgCode() + list[0] + " ")
            list.allStates().forEachIndexed { index, state ->
                print(stateColor(state).fgCode() + list[index + 1] + " ")
            }
            println(AnsiColor.DEFAULT.fgCode())
        }
    }

    private fun List<Int>.allStates(): Sequence<State> =
        asSequence().windowed(3).runningFold(State.First) { acc, window ->
            acc.nextState(window)
        }

    private fun State.nextState(window: List<Int>) = nextState(window[0], window[1], window[2])

    private fun stateColor(state: State) = when (state) {
        State.First -> AnsiColor.BRIGHT_BLACK
        State.KnownIncreasing -> AnsiColor.BRIGHT_MAGENTA
        State.KnownDecreasing -> AnsiColor.BRIGHT_CYAN
        State.KnownIncreasingDroppedPreviousOrEarlier -> AnsiColor.MAGENTA
        State.KnownDecreasingDroppedPreviousOrEarlier -> AnsiColor.CYAN
        State.KnownIncreasingDroppedPrevious -> AnsiColor.MAGENTA
        State.KnownDecreasingDroppedPrevious -> AnsiColor.CYAN
        State.DroppedPreviousForIncreaseOrEarlierForDecrease -> AnsiColor.WHITE
        State.DroppedPreviousForDecreaseOrEarlierForIncrease -> AnsiColor.WHITE
        State.DroppedEarlierDirectionUnknown -> AnsiColor.WHITE
        State.KnownIncreasingDroppedEarlier -> AnsiColor.MAGENTA
        State.KnownDecreasingDroppedEarlier -> AnsiColor.CYAN
        State.Invalid -> AnsiColor.RED
    }

    enum class State(val isValidPart1: Boolean) {
        First(true) {
            override fun nextState(v0: Int, v1: Int, v2: Int): State =
                when {
                    !isInRange(v0, v1) -> dropV0orV1(v0, v1, v2)
                    v1 > v0 -> startedWithIncrease(v0, v1, v2)
                    else -> startedWithDecrease(v0, v1, v2)
                }

            private fun dropV0orV1(v0: Int, v1: Int, v2: Int): State =
                when {
                    !isInRange(v0, v2) -> when {
                        !isInRange(v1, v2) -> Invalid
                        v1 < v2 -> KnownIncreasingDroppedEarlier
                        else -> KnownDecreasingDroppedEarlier
                    }

                    v0 < v2 ->
                        if (isInRange(v1, v2) && v1 > v2)
                            DroppedEarlierDirectionUnknown
                        else
                            KnownIncreasingDroppedEarlier

                    else ->
                        if (isInRange(v1, v2) && v1 < v2)
                            DroppedEarlierDirectionUnknown
                        else
                            KnownDecreasingDroppedEarlier
                }

            private fun startedWithIncrease(v0: Int, v1: Int, v2: Int): State =
                when {
                    isInRange(v1, v2) && (v1 < v2) -> KnownIncreasing
                    isInRange(v1, v2) -> DroppedPreviousForIncreaseOrEarlierForDecrease
                    isInRange(v0, v2) && v0 > v2 -> DroppedPreviousForIncreaseOrEarlierForDecrease
                    else -> KnownIncreasingDroppedPrevious
                }

            private fun startedWithDecrease(v0: Int, v1: Int, v2: Int): State =
                when {
                    isInRange(v1, v2) && v1 > v2 -> KnownDecreasing
                    isInRange(v1, v2) -> DroppedPreviousForDecreaseOrEarlierForIncrease
                    isInRange(v0, v2) && v0 < v2 -> DroppedPreviousForDecreaseOrEarlierForIncrease
                    else -> KnownDecreasingDroppedPrevious
                }
        },
        KnownIncreasing(true) {
            override fun nextState(v0: Int, v1: Int, v2: Int): State =
                when {
                    isInRange(v1, v2) && v1 < v2 -> KnownIncreasing
                    isInRange(v0, v2) && v0 < v2 -> KnownIncreasingDroppedPreviousOrEarlier
                    else -> KnownIncreasingDroppedPrevious
                }
        },
        KnownDecreasing(true) {
            override fun nextState(v0: Int, v1: Int, v2: Int): State =
                when {
                    isInRange(v1, v2) && v1 > v2 -> KnownDecreasing
                    isInRange(v0, v2) && v0 > v2 -> KnownDecreasingDroppedPreviousOrEarlier
                    else -> KnownDecreasingDroppedPrevious
                }
        },
        KnownIncreasingDroppedPreviousOrEarlier(false) {
            override fun nextState(v0: Int, v1: Int, v2: Int): State =
                if ((isInRange(v1, v2) && v1 < v2) || (isInRange(v0, v2) && v0 < v2))
                    KnownIncreasingDroppedEarlier
                else
                    Invalid
        },
        KnownDecreasingDroppedPreviousOrEarlier(false) {
            override fun nextState(v0: Int, v1: Int, v2: Int): State =
                if ((isInRange(v1, v2) && v1 > v2) || (isInRange(v0, v2) && v0 > v2))
                    KnownDecreasingDroppedEarlier
                else
                    Invalid
        },
        KnownIncreasingDroppedPrevious(false) {
            override fun nextState(v0: Int, v1: Int, v2: Int): State =
                if (isInRange(v0, v2) && v0 < v2)
                    KnownIncreasingDroppedEarlier
                else
                    Invalid
        },
        KnownDecreasingDroppedPrevious(false) {
            override fun nextState(v0: Int, v1: Int, v2: Int): State =
                if (isInRange(v0, v2) && v0 > v2)
                    KnownDecreasingDroppedEarlier
                else
                    Invalid
        },
        DroppedPreviousForIncreaseOrEarlierForDecrease(false) {
            override fun nextState(v0: Int, v1: Int, v2: Int): State {
                val prevPrevOk = isInRange(v0, v2) && v0 < v2
                val prevOk = isInRange(v1, v2) && v1 > v2
                return when {
                    prevPrevOk && prevOk -> DroppedEarlierDirectionUnknown
                    prevPrevOk -> KnownIncreasingDroppedEarlier
                    prevOk -> KnownDecreasingDroppedEarlier
                    else -> Invalid
                }
            }
        },
        DroppedPreviousForDecreaseOrEarlierForIncrease(false) {
            override fun nextState(v0: Int, v1: Int, v2: Int): State {
                val prevPrevOk = isInRange(v0, v2) && v0 > v2
                val prevOk = isInRange(v1, v2) && v1 < v2
                return when {
                    prevPrevOk && prevOk -> DroppedEarlierDirectionUnknown
                    prevPrevOk -> KnownDecreasingDroppedEarlier
                    prevOk -> KnownIncreasingDroppedEarlier
                    else -> Invalid
                }
            }
        },
        DroppedEarlierDirectionUnknown(false) {
            override fun nextState(v0: Int, v1: Int, v2: Int): State =
                when {
                    !isInRange(v1, v2) -> Invalid
                    v1 < v2 -> KnownIncreasingDroppedEarlier
                    else -> KnownDecreasingDroppedEarlier
                }
        },
        KnownIncreasingDroppedEarlier(false) {
            override fun nextState(v0: Int, v1: Int, v2: Int): State =
                if (isInRange(v1, v2) && v1 < v2)
                    KnownIncreasingDroppedEarlier
                else
                    Invalid
        },
        KnownDecreasingDroppedEarlier(false) {
            override fun nextState(v0: Int, v1: Int, v2: Int): State =
                if (isInRange(v1, v2) && v1 > v2)
                    KnownDecreasingDroppedEarlier
                else
                    Invalid
        },
        Invalid(false) {
            override fun nextState(v0: Int, v1: Int, v2: Int): State = Invalid
        };

        abstract fun nextState(v0: Int, v1: Int, v2: Int): State
    }

    private fun isInRange(a: Int, b: Int) = abs(a - b) in 1..3
}
