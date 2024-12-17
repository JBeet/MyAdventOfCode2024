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
        val out = mutableListOf<Int>()
        Computer(a, b, c, program) {
            out.add(it)
        }.execute()
        return out.joinToString(",")
    }

    fun part2(input: String): Long {
        val (sRegisters, sProgram) = input.paragraphs()
        val (a, b, c) = sRegisters.lines().map { it.longsOrZero().first() }
        val program = sProgram.intsOrZero()
        return naturalNumberLongs.filter { newRegA ->
            var i = 0
            val computer = Computer(newRegA, b, c, program) {
                if (i > 7)
                    println("newRegA: $newRegA checking $it at position $i")
                i < program.size && (program[i++] == it)
            }
            computer.execute()
//            println(computer.ip)
            i == program.size && computer.ip != Int.MAX_VALUE
        }.first()
    }

    class Computer(var a: Long, var b: Long, var c: Long, val program: List<Int>, val out: (Int) -> Boolean) {
        var ip = 0

        fun execute() {
            while (ip < program.size - 1) {
                val instruction = Instruction.entries[program[ip]]
                execute(instruction, program[ip + 1])
            }
        }

        fun part2(input: String): Int {
            TODO("Not yet implemented")
        }

        private fun execute(instruction: Instruction, operand: Int) {
            when (instruction) {
                Instruction.ADV -> {
                    a = a shr comboValue(operand).toInt()
                    ip += 2
                }

                Instruction.BXL -> {
                    b = b xor operand.toLong()
                    ip += 2
                }

                Instruction.BST -> {
                    b = comboValue(operand) % 8
                    ip += 2
                }

                Instruction.JNZ -> {
                    if (a != 0L)
                        ip = operand
                    else
                        ip += 2
                }

                Instruction.BXC -> {
                    b = b xor c
                    ip += 2
                }

                Instruction.OUT -> {
                    val result = out(comboValue(operand).toInt() % 8)
                    if (result)
                        ip += 2
                    else
                        ip = Int.MAX_VALUE
                }

                Instruction.BDV -> {
                    b = a shr comboValue(operand).toInt()
                    ip += 2
                }

                Instruction.CDV -> {
                    c = a shr comboValue(operand).toInt()
                    ip += 2
                }
            }
        }

        private fun comboValue(operand: Int): Long =
            when (operand) {
                0, 1, 2, 3 -> operand.toLong()
                4 -> a
                5 -> b
                6 -> c
                else -> error("Unexpected combo operand $operand")
            }

        enum class Instruction {
            ADV, BXL, BST, JNZ, BXC, OUT, BDV, CDV;
        }
    }
}
