import kotlin.math.absoluteValue

fun main() {
    val testInput = readInput("Day02_test")
    check(Day02WithStateEnum.part1(testInput) == 2) { "Expected 2 but was " + Day02WithStateEnum.part1(testInput) }
    val input = readInput("Day02")
    println(Day02WithStateEnum.part1(input))
    check(Day02WithStateEnum.part2(testInput) == 4) { "Expected 4 but was " + Day02WithStateEnum.part2(testInput) }
    println(Day02WithStateEnum.part2(input))
}

object Day02WithStateEnum {
    fun part1(input: String): Int =
        input.intLists().count { list -> list.stateSequence().isValidPart1 }

    fun part2(input: String) =
        input.intLists().count { list -> list.stateSequence() != State.Invalid }

    private fun List<Int>.stateSequence(): State {
        println()
        print(stateColor(State.First).fgCode())
        print(this[0])
        print(" ")
        print(this[1])
        print(" ")
        return asSequence().windowed(3).fold(State.First) { acc, window ->
            acc.nextState(window[0], window[1], window[2]).also {
                print(stateColor(it).fgCode())
                print(window[2])
                print(" ")
            }
        }
    }

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
            override fun nextState(prevPrevValue: Int, prevValue: Int, i: Int): State = when {
                !isInRange(prevPrevValue, prevValue) -> dropFirstOrSecond(prevPrevValue, prevValue, i)
                prevValue > prevPrevValue -> startedWithIncrease(prevPrevValue, prevValue, i)
                else -> startedWithDecrease(prevPrevValue, prevValue, i)
            }

            private fun dropFirstOrSecond(prevPrevValue: Int, prevValue: Int, i: Int): State {
                val prevInRange = isInRange(prevValue, i)
                return when {
                    !isInRange(prevPrevValue, i) -> when {
                        !prevInRange -> Invalid
                        i > prevValue -> KnownIncreasingDroppedEarlier
                        else -> KnownDecreasingDroppedEarlier
                    }

                    i > prevPrevValue ->
                        if (prevInRange && i < prevValue)
                            DroppedEarlierDirectionUnknown
                        else
                            KnownIncreasingDroppedEarlier

                    else ->
                        if (prevInRange && i > prevValue)
                            DroppedEarlierDirectionUnknown
                        else
                            KnownDecreasingDroppedEarlier
                }
            }

            private fun startedWithIncrease(prevPrevValue: Int, prevValue: Int, i: Int): State {
                val prevInRange = isInRange(prevValue, i)
                return when {
                    prevInRange && (i > prevValue) -> KnownIncreasing
                    prevInRange -> DroppedPreviousForIncreaseOrEarlierForDecrease
                    isInRange(i, prevPrevValue) && i < prevPrevValue -> DroppedPreviousForIncreaseOrEarlierForDecrease
                    else -> KnownIncreasingDroppedPrevious
                }
            }

            private fun startedWithDecrease(prevPrevValue: Int, prevValue: Int, i: Int): State {
                val prevInRange = isInRange(prevValue, i)
                return when {
                    prevInRange && i < prevValue -> KnownDecreasing
                    prevInRange -> DroppedPreviousForDecreaseOrEarlierForIncrease
                    isInRange(prevPrevValue, i) && i > prevPrevValue -> DroppedPreviousForDecreaseOrEarlierForIncrease
                    else -> KnownDecreasingDroppedPrevious
                }
            }
        },
        KnownIncreasing(true) {
            override fun nextState(prevPrevValue: Int, prevValue: Int, i: Int): State = when {
                isInRange(i, prevValue) && i > prevValue -> KnownIncreasing
                isInRange(i, prevPrevValue) && i > prevPrevValue -> KnownIncreasingDroppedPreviousOrEarlier
                else -> KnownIncreasingDroppedPrevious
            }
        },
        KnownDecreasing(true) {
            override fun nextState(prevPrevValue: Int, prevValue: Int, i: Int): State = when {
                isInRange(i, prevValue) && i < prevValue -> KnownDecreasing
                isInRange(i, prevPrevValue) && i < prevPrevValue -> KnownDecreasingDroppedPreviousOrEarlier
                else -> KnownDecreasingDroppedPrevious
            }
        },
        KnownIncreasingDroppedPreviousOrEarlier(false) {
            override fun nextState(prevPrevValue: Int, prevValue: Int, i: Int): State =
                if ((isInRange(i, prevValue) && i > prevValue) || (isInRange(i, prevPrevValue) && i > prevPrevValue))
                    KnownIncreasingDroppedEarlier
                else
                    Invalid
        },
        KnownDecreasingDroppedPreviousOrEarlier(false) {
            override fun nextState(prevPrevValue: Int, prevValue: Int, i: Int): State =
                if ((isInRange(i, prevValue) && i < prevValue) || (isInRange(i, prevPrevValue) && i < prevPrevValue))
                    KnownDecreasingDroppedEarlier
                else
                    Invalid
        },
        KnownIncreasingDroppedPrevious(false) {
            override fun nextState(prevPrevValue: Int, prevValue: Int, i: Int): State =
                if (isInRange(i, prevPrevValue) && i > prevPrevValue)
                    KnownIncreasingDroppedEarlier
                else
                    Invalid
        },
        KnownDecreasingDroppedPrevious(false) {
            override fun nextState(prevPrevValue: Int, prevValue: Int, i: Int): State =
                if (isInRange(i, prevPrevValue) && i < prevPrevValue)
                    KnownDecreasingDroppedEarlier
                else
                    Invalid
        },
        DroppedPreviousForIncreaseOrEarlierForDecrease(false) {
            override fun nextState(prevPrevValue: Int, prevValue: Int, i: Int): State {
                val prevPrevOk = isInRange(i, prevPrevValue) && i > prevPrevValue
                val prevOk = isInRange(i, prevValue) && i < prevValue
                return when {
                    prevPrevOk && prevOk -> DroppedEarlierDirectionUnknown
                    prevPrevOk -> KnownIncreasingDroppedEarlier
                    prevOk -> KnownDecreasingDroppedEarlier
                    else -> Invalid
                }
            }
        },
        DroppedPreviousForDecreaseOrEarlierForIncrease(false) {
            override fun nextState(prevPrevValue: Int, prevValue: Int, i: Int): State {
                val prevPrevOk = isInRange(i, prevPrevValue) && i < prevPrevValue
                val prevOk = isInRange(i, prevValue) && i > prevValue
                return when {
                    prevPrevOk && prevOk -> DroppedEarlierDirectionUnknown
                    prevPrevOk -> KnownDecreasingDroppedEarlier
                    prevOk -> KnownIncreasingDroppedEarlier
                    else -> Invalid
                }
            }
        },
        DroppedEarlierDirectionUnknown(false) {
            override fun nextState(prevPrevValue: Int, prevValue: Int, i: Int): State = when {
                !isInRange(i, prevValue) -> Invalid
                i > prevValue -> KnownIncreasingDroppedEarlier
                else -> KnownDecreasingDroppedEarlier
            }
        },
        KnownIncreasingDroppedEarlier(false) {
            override fun nextState(prevPrevValue: Int, prevValue: Int, i: Int): State =
                if (isInRange(i, prevValue) && i > prevValue)
                    KnownIncreasingDroppedEarlier
                else
                    Invalid
        },
        KnownDecreasingDroppedEarlier(false) {
            override fun nextState(prevPrevValue: Int, prevValue: Int, i: Int): State =
                if (isInRange(i, prevValue) && i < prevValue)
                    KnownDecreasingDroppedEarlier
                else
                    Invalid
        },
        Invalid(false) {
            override fun nextState(prevPrevValue: Int, prevValue: Int, i: Int): State = Invalid
        };

        abstract fun nextState(prevPrevValue: Int, prevValue: Int, i: Int): State
    }

    private fun isInRange(a: Int, b: Int) = (a - b).absoluteValue in 1..3
}
