import utils.*


fun main() {
    val testInput = readInput("Day17_test")
    verify("4,6,3,5,6,3,5,2,1,0", Day17.part1(testInput))
    println("Test input OK")
    val input = readInput("Day17")
    println(Day17.part1(input))
    val testInput2 = readInput("Day17_test2")
    verify(117440, Day17.part2(testInput2))
    println("Test input OK")
    println(Day17.part2(input))
}

object Day17 {
    fun part1(input: String): String {
        val (sRegisters, sProgram) = input.paragraphs()
        val (a, b, c) = sRegisters.lines().map { it.longsOrZero().first() }
        val program = sProgram.intsOrZero()
        val computer = Computer(program) { true }
        val completed = computer.execute(Registers(a, b, c)).toList()
        return completed.first().output.joinToString(",") { (it as Value.Known).v.toString() }
    }

    fun part2(input: String): Long {
        val (sRegisters, sProgram) = input.paragraphs()
        val (_, b, c) = sRegisters.lines().map { it.longsOrZero().first() }
        val program = sProgram.intsOrZero()
        val computer = Computer(program) { it.output.size <= program.size }
        val a = Value.Unknown(CalculatedValue.InitialValue)
        val completed = computer.execute(Registers(a, b, c)).filter { it.output.size == program.size }.toList()
        val equations =
            completed.first().output.filterIsInstance<Value.Unknown>()
                .mapIndexed { index, v -> v.calculation to program[index] }
        return filteredBinaryNumberSequence(equations) { (cv, target), value ->
            matches(cv, value, target)
        }.sequence().first()
    }

    private fun <T> filteredBinaryNumberSequence(filters: Iterable<T>, filter: (T, Long) -> Boolean): PossibleNumbers =
        filters.fold(PossibleNumbers.Repeat(setOf(0L), 1L)) { src, f ->
            buildWithEquation(src) { filter(f, it) }
        }

    private fun buildWithEquation(src: PossibleNumbers.Repeat, filter: (Long) -> Boolean): PossibleNumbers.Repeat {
        val first = src.base.filter(filter)
        if (first.isEmpty())
            return buildWithEquation(src.prepareCycles(2), filter)
        if ((0L..255L).all { cycleNr -> // check 256 cycles
                val tested = src.base.addToAll(cycleNr * src.cycleLength).filter(filter)
                tested.size == first.size && tested.addToAll(-cycleNr * src.cycleLength) == first
            }) {
            return PossibleNumbers.Repeat(first.toSet(), src.cycleLength)
        }
        return buildWithEquation(src.prepareCycles(2), filter)
    }

    private fun matches(cv: CalculatedValue, value: Long, target: Int) =
        calculate(cv, value) == target.toLong()

    private fun calculate(cv: CalculatedValue, value: Long): Long = when (cv) {
        CalculatedValue.InitialValue -> value
        is CalculatedValue.IsZero -> calculate(cv.base, value)
        is CalculatedValue.Mod8 -> calculate(cv.base, value) % 8
        is CalculatedValue.ShrKnown -> calculate(cv.base, value) shr cv.operand
        is CalculatedValue.ShrUnknown -> calculate(cv.base, value) shr calculate(cv.operand, value).toInt()
        is CalculatedValue.XorKnown -> calculate(cv.base, value) xor cv.operand
        is CalculatedValue.XorUnknown -> calculate(cv.base, value) xor calculate(cv.operand, value)
    }

    sealed interface CalculatedValue {
        fun xor(v: Long): CalculatedValue = XorKnown(this, v)

        data object InitialValue : CalculatedValue

        data class XorKnown(val base: CalculatedValue, val operand: Long) : CalculatedValue {
            override fun xor(v: Long): CalculatedValue = XorKnown(base, operand xor v)
            override fun toString(): String = "($base xor $operand)"
        }

        class XorUnknown(val base: CalculatedValue, val operand: CalculatedValue) : CalculatedValue {
            override fun toString(): String = "($base xor $operand)"
        }

        data class ShrKnown(val base: CalculatedValue, val operand: Int) : CalculatedValue {
            override fun toString(): String = "($base shr $operand)"
        }

        data class ShrUnknown(val base: CalculatedValue, val operand: CalculatedValue) : CalculatedValue {
            override fun toString(): String = "($base shr $operand)"
        }

        class Mod8(val base: CalculatedValue) : CalculatedValue {
            override fun toString(): String = "($base mod 8)"
        }

        class IsZero(val base: CalculatedValue, private val isZero: Boolean) : CalculatedValue {
            override fun toString(): String = if (isZero) "($base == 0)" else "($base != 0)"
        }
    }

    sealed interface Value {
        infix fun shr(o: Value): Value
        infix fun xor(o: Value): Value
        fun mod8(): Value
        fun <T> ifZero(ifZero: (Value) -> T, ifNotZero: (Value) -> T): List<T>

        @JvmInline
        value class Known(val v: Long) : Value {
            constructor(i: Int) : this(i.toLong())

            override fun shr(o: Value) = when (o) {
                is Known -> Known(v shr o.v.toInt())
                is Unknown -> TODO()
            }

            override fun xor(o: Value): Value = when (o) {
                is Known -> Known(v xor o.v)
                is Unknown -> TODO()
            }

            override fun mod8(): Value = Known(v % 8)
            override fun <T> ifZero(ifZero: (Value) -> T, ifNotZero: (Value) -> T): List<T> =
                if (v == 0L) listOf(ifZero(this)) else listOf(ifNotZero(this))
        }

        class Unknown(val calculation: CalculatedValue) : Value {
            override fun shr(o: Value): Value = when (o) {
                is Known -> Unknown(CalculatedValue.ShrKnown(calculation, o.v.toInt()))
                is Unknown -> Unknown(CalculatedValue.ShrUnknown(calculation, o.calculation))
            }

            override fun xor(o: Value): Value = when (o) {
                is Known -> Unknown(calculation.xor(o.v))
                is Unknown -> Unknown(CalculatedValue.XorUnknown(calculation, o.calculation))
            }

            override fun mod8(): Value = Unknown(CalculatedValue.Mod8(calculation))

            override fun <T> ifZero(ifZero: (Value) -> T, ifNotZero: (Value) -> T): List<T> {
                return listOf(
                    ifZero(Unknown(CalculatedValue.IsZero(calculation, true))),
                    ifNotZero(Unknown(CalculatedValue.IsZero(calculation, false)))
                )
            }

            override fun toString(): String = "Unknown[$calculation]"
        }
    }

    enum class Instruction {
        ADV, BXL, BST, JNZ, BXC, OUT, BDV, CDV;
    }

    data class Registers(
        val a: Value,
        val b: Value,
        val c: Value,
        val ip: Int,
        val output: List<Value>
    ) {
        constructor(a: Long, b: Long, c: Long) : this(Value.Known(a), b, c)
        constructor(a: Value, b: Long, c: Long) : this(a, Value.Known(b), Value.Known(c), 0, emptyList())

        fun execute(instruction: Instruction, operand: Int) = sequence {
            when (instruction) {
                Instruction.ADV -> yield(copy(ip = ip + 2, a = a shr comboValue(operand)))
                Instruction.BXL -> yield(copy(ip = ip + 2, b = b xor Value.Known(operand)))
                Instruction.BST -> yield(copy(ip = ip + 2, b = comboValue(operand).mod8()))
                Instruction.JNZ -> yieldAll(
                    a.ifZero(
                        { copy(a = it, ip = ip + 2) },
                        { copy(a = it, ip = operand) })
                )

                Instruction.BXC -> yield(copy(ip = ip + 2, b = b xor c))
                Instruction.OUT -> yield(copy(ip = ip + 2, output = output + comboValue(operand).mod8()))
                Instruction.BDV -> yield(copy(ip = ip + 2, b = a shr comboValue(operand)))
                Instruction.CDV -> yield(copy(ip = ip + 2, c = a shr comboValue(operand)))
            }
        }

        private fun comboValue(operand: Int): Value =
            when (operand) {
                0, 1, 2, 3 -> Value.Known(operand)
                4 -> a
                5 -> b
                6 -> c
                else -> error("Unexpected combo operand $operand")
            }
    }

    class Computer(private val program: List<Int>, private val verify: (Registers) -> Boolean) {
        fun execute(registers: Registers) = sequence {
            val activeThreads = mutableListOf(registers)
            while (activeThreads.isNotEmpty()) {
                val thread = activeThreads.removeFirst()
                if (thread.ip < program.size - 1) {
                    val instruction = Instruction.entries[program[thread.ip]]
                    thread.execute(instruction, program[thread.ip + 1]).forEach {
                        if (verify(it))
                            activeThreads.add(it)
                    }
                } else
                    yield(thread)
            }
        }
    }
}
