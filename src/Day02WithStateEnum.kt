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
    fun part1(input: String): Int = input.intLists().count { it.isSafe() }
    private fun List<Int>.isSafe() =
        this.hasValidOrder() && zipWithNext { a, b -> (a - b).absoluteValue }.all { it in 1..3 }

    private fun List<Int>.hasValidOrder() = this == sorted() || this == sortedDescending()

    fun part2(input: String) =
        input.intLists().count { original ->
            original.asSequence().windowed(3).runningFold(State.First) { acc, window ->
                acc.nextState(window[0], window[1], window[2])
            }.none { it == State.Invalid }
        }

    enum class State {
        First {
            override fun nextState(prevPrevValue: Int, prevValue: Int, i: Int): State = when {
                !isInRange(prevValue, prevPrevValue) -> dampenImmediately(prevPrevValue, prevValue, i)
                prevValue > prevPrevValue -> startedWithIncrease(prevPrevValue, prevValue, i)
                else -> startedWithDecrease(prevPrevValue, prevValue, i)
            }

            private fun dampenImmediately(prevPrevValue: Int, prevValue: Int, i: Int): State = when {
                !isInRange(prevPrevValue, i) -> when {
                    !isInRange(prevValue, i) -> Invalid
                    i > prevValue -> IncreasingDampened
                    else -> DecreasingDampened
                }

                i > prevPrevValue -> when {
                    !isInRange(prevValue, i) || i > prevValue -> IncreasingDampened
                    else -> NoDirectionDampened
                }

                else -> when {
                    !isInRange(prevValue, i) -> DecreasingDampened
                    i > prevValue -> NoDirectionDampened
                    else -> DecreasingDampened
                }
            }

            private fun startedWithIncrease(prevPrevValue: Int, prevValue: Int, i: Int): State {
                val prevInRange = isInRange(prevValue, i)
                return when {
                    prevInRange && (i > prevValue) -> IncreasingWithDampened
                    prevInRange -> IncreasingDecreasingDampened
                    isInRange(i, prevPrevValue) && i < prevPrevValue -> IncreasingDecreasingDampened
                    else -> PrevIncreasingDampened
                }
            }

            private fun startedWithDecrease(prevPrevValue: Int, prevValue: Int, i: Int): State {
                val prevInRange = isInRange(prevValue, i)
                return when {
                    prevInRange && i < prevValue -> DecreasingWithDampened
                    prevInRange -> DecreasingIncreasingDampened
                    isInRange(i, prevPrevValue) && i > prevPrevValue -> DecreasingIncreasingDampened
                    else -> PrevDecreasingDampened
                }
            }
        },
        PrevIncreasingDampened {
            override fun nextState(prevPrevValue: Int, prevValue: Int, i: Int): State =
                if (isInRange(i, prevPrevValue) && i > prevPrevValue) IncreasingDampened else Invalid
        },
        IncreasingDampened {
            override fun nextState(prevPrevValue: Int, prevValue: Int, i: Int): State =
                if (isInRange(i, prevValue) && i > prevValue) IncreasingDampened else Invalid
        },
        IncreasingDecreasingDampened {
            override fun nextState(prevPrevValue: Int, prevValue: Int, i: Int): State {
                val prevPrevOk = isInRange(i, prevPrevValue) && i > prevPrevValue
                val prevOk = isInRange(i, prevValue) && i < prevValue
                return when {
                    prevPrevOk && prevOk -> NoDirectionDampened
                    prevPrevOk -> IncreasingDampened
                    prevOk -> DecreasingDampened
                    else -> Invalid
                }
            }
        },
        DecreasingIncreasingDampened {
            override fun nextState(prevPrevValue: Int, prevValue: Int, i: Int): State {
                val prevPrevOk = isInRange(i, prevPrevValue) && i < prevPrevValue
                val prevOk = isInRange(i, prevValue) && i > prevValue
                return when {
                    prevPrevOk && prevOk -> NoDirectionDampened
                    prevPrevOk -> DecreasingDampened
                    prevOk -> IncreasingDampened
                    else -> Invalid
                }
            }
        },
        PrevDecreasingDampened {
            override fun nextState(prevPrevValue: Int, prevValue: Int, i: Int): State =
                if (isInRange(i, prevPrevValue) && i < prevPrevValue) DecreasingDampened else Invalid
        },
        DecreasingDampened {
            override fun nextState(prevPrevValue: Int, prevValue: Int, i: Int): State =
                if (isInRange(i, prevValue) && i < prevValue) DecreasingDampened else Invalid
        },
        IncreasingWithDampened {
            override fun nextState(prevPrevValue: Int, prevValue: Int, i: Int): State = when {
                isInRange(i, prevValue) && i > prevValue -> IncreasingWithDampened
                isInRange(i, prevPrevValue) && i > prevPrevValue -> IncreasingDampened2X
                else -> PrevIncreasingDampened
            }
        },
        DecreasingWithDampened {
            override fun nextState(prevPrevValue: Int, prevValue: Int, i: Int): State = when {
                isInRange(i, prevValue) && i < prevValue -> DecreasingWithDampened
                isInRange(i, prevPrevValue) && i < prevPrevValue -> DecreasingDampened2X
                else -> PrevDecreasingDampened
            }
        },
        IncreasingDampened2X {
            override fun nextState(prevPrevValue: Int, prevValue: Int, i: Int): State =
                if ((isInRange(i, prevValue) && i > prevValue) || (isInRange(i, prevPrevValue) && i > prevPrevValue))
                    IncreasingDampened
                else
                    Invalid
        },
        DecreasingDampened2X {
            override fun nextState(prevPrevValue: Int, prevValue: Int, i: Int): State =
                if ((isInRange(i, prevValue) && i < prevValue) || (isInRange(i, prevPrevValue) && i < prevPrevValue))
                    DecreasingDampened
                else
                    Invalid
        },
        NoDirectionDampened {
            override fun nextState(prevPrevValue: Int, prevValue: Int, i: Int): State = when {
                !isInRange(i, prevValue) -> Invalid
                i > prevValue -> IncreasingDampened
                else -> DecreasingDampened
            }
        },
        Invalid {
            override fun nextState(prevPrevValue: Int, prevValue: Int, i: Int): State = Invalid
        };

        abstract fun nextState(prevPrevValue: Int, prevValue: Int, i: Int): State
    }

    private fun isInRange(a: Int, b: Int) = (a - b).absoluteValue in 1..3
}
