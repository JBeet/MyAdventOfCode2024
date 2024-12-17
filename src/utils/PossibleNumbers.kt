package utils

sealed interface PossibleNumbers {
    operator fun contains(value: Long): Boolean
    fun sequence(): Sequence<Long>
    fun restrict(numbers: PossibleNumbers): PossibleNumbers

    data class Single(val value: Long) : PossibleNumbers {
        override fun contains(value: Long): Boolean = value == this.value
        override fun sequence() = sequenceOf(value)
        override fun restrict(numbers: PossibleNumbers): PossibleNumbers = when (numbers) {
            All -> this
            Empty -> Empty
            is Single -> if (value == numbers.value) this else Empty
            is Limited -> if (value in numbers) this else Empty
            is Repeat -> if (value in numbers) this else Empty
        }
    }

    data class Limited(val values: Set<Long>) : PossibleNumbers {
        init {
            check(values.isNotEmpty())
        }

        override fun contains(value: Long) = value in values
        override fun sequence() = values.asSequence()
        override fun restrict(numbers: PossibleNumbers): PossibleNumbers = when (numbers) {
            All -> this
            Empty -> Empty
            is Single -> if (numbers.value in values) numbers else Empty
            is Limited -> Limited(values.intersect(numbers.values))
            is Repeat -> numbers.restrict(this)
        }
    }

    data class Repeat(val base: Set<Long>, val cycleLength: Long) : PossibleNumbers {
        init {
            check(base.isNotEmpty())
            check(base.all { it in 0..<cycleLength }) { "Not all of $base < $cycleLength" }
        }

        override fun contains(value: Long): Boolean = (value % cycleLength) in base
        override fun sequence() = naturalNumberLongs.flatMap {
            base.addToAll(it * cycleLength)
        }

        override fun restrict(numbers: PossibleNumbers): PossibleNumbers = when (numbers) {
            All -> this
            Empty -> Empty
            is Single -> if (numbers.value in this) numbers else Empty
            is Limited -> if (numbers.values.all { it < cycleLength }) {
                Repeat(base.intersect(numbers.values), cycleLength)
            } else TODO("restrict $this with $numbers")

            is Repeat -> restrict(numbers)
        }

        private fun restrict(numbers: Repeat): Repeat {
            val lcm = leastCommonMultiple(cycleLength, numbers.cycleLength)
            val fromThis = LongProgression.fromClosedRange(0, lcm - 1, cycleLength)
                .flatMapTo(mutableSetOf()) { c -> base.map { b -> b + c } }
            val fromNrs = LongProgression.fromClosedRange(0, lcm - 1, numbers.cycleLength)
                .flatMapTo(mutableSetOf()) { c -> numbers.base.map { b -> b + c } }
            return Repeat(fromThis.intersect(fromNrs), lcm)
        }

        fun prepareCycles(count: Int): Repeat {
            check(count > 1)
            val newBase = base.toMutableSet()
            (1..<count).forEach { cycleNr -> newBase.addAll(base.addToAll(cycleNr * cycleLength)) }
            return Repeat(newBase, count * cycleLength)
        }
    }

    data object All : PossibleNumbers {
        override fun contains(value: Long): Boolean = true
        override fun sequence(): Sequence<Long> = naturalNumberLongs
        override fun restrict(numbers: PossibleNumbers): PossibleNumbers = numbers
    }

    data object Empty : PossibleNumbers {
        override fun contains(value: Long): Boolean = false
        override fun sequence(): Sequence<Long> = emptySequence()
        override fun restrict(numbers: PossibleNumbers): PossibleNumbers = Empty
    }
}
