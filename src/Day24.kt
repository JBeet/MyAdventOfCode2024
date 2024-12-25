import Day24.Wire.*
import utils.paragraphs
import utils.readInput
import utils.removeFirst
import utils.verify

fun main() {
    val testInput = readInput("Day24_test")
    verify(2024, Day24.part1(testInput))
    println("Test input OK")
    val input = readInput("Day24")
    println(Day24.part1(input))
    println(Day24.part2(input))

}

object Day24 {
    sealed interface Wire {
        data class Input(val prefix: String, val index: Int) : Wire, Comparable<Input> {
            override fun compareTo(other: Input): Int {
                val vc = index.compareTo(other.index)
                return if (vc != 0) vc else prefix.compareTo(other.prefix)
            }

            override fun toString(): String = "`$prefix$index`"
        }

        data class ZOutput(val index: Int) : Wire, Comparable<ZOutput> {
            override fun compareTo(other: ZOutput): Int = index.compareTo(other.index)
            override fun toString(): String = "`z$index`"
        }

        data class Other(val name: String) : Wire {
            override fun toString(): String = "`$name`"
        }
    }

    private fun Input(name: String): Input = when {
        name.startsWith("x") -> Input("x", name.drop(1).toInt())
        name.startsWith("y") -> Input("y", name.drop(1).toInt())
        else -> error("Not an input wire: $name")
    }

    private fun Wire(name: String): Wire = when {
        name.startsWith("x") -> Input("x", name.drop(1).toInt())
        name.startsWith("y") -> Input("y", name.drop(1).toInt())
        name.startsWith("z") -> ZOutput(name.drop(1).toInt())
        else -> Other(name)
    }

    enum class GateType {
        XOR, AND, OR
    }

    data class Gate(val inp1: Wire, val type: GateType, val inp2: Wire, val out: Wire)

    data class Circuit(val inputs: Map<Input, Boolean>, val gates: List<Gate>) {
        private val indexedByOutput = gates.associateBy { it.out }
        private val outputGates = indexedByOutput.keys.filterIsInstance<ZOutput>().sorted()
        private val wireValues = inputs.toMutableMap<Wire, Boolean>()
        private val indexedByInput1 = gates.groupBy { it.inp1 }
        private val indexedByInput2 = gates.groupBy { it.inp2 }
        private val indexedByInput = (indexedByInput1.keys + indexedByInput2.keys).associateWith { key ->
            (indexedByInput1.getOrDefault(key, emptyList()) + indexedByInput2.getOrDefault(key, emptyList()))
        }.mapValues { (_, dependants) -> dependants.sortedBy { it.type } }

        init {
            check(indexedByOutput.size == gates.size) { "Some outputs serviced by multiple gates" }
        }

        fun part1(): Long = outputGates.fold(1L to 0L) { (multiplier, value), wire ->
            multiplier * 2 to (if (valueFor(wire)) value + multiplier else value)
        }.second

        private fun valueFor(wire: Wire): Boolean = wireValues.getOrPut(wire) {
            val gate = checkNotNull(indexedByOutput[wire]) { "No output for $wire" }
            when (gate.type) {
                GateType.AND -> valueFor(gate.inp1) && valueFor(gate.inp2)
                GateType.OR -> valueFor(gate.inp1) || valueFor(gate.inp2)
                GateType.XOR -> valueFor(gate.inp1) xor valueFor(gate.inp2)
            }
        }

        private fun testOutput(wire: ZOutput): List<Wire> {
            val gate = indexedByOutput[wire]
            checkNotNull(gate) { "no output for $wire" }
            val problemWires = testGate(gate, wire.index, emptyList())
            if (problemWires.isEmpty()) {
                val b = !copy(inputs = inputs.keys.associateWith { it.prefix == "x" }).valueFor(wire)
                if (b) {
                    println("Problem not identified but exists for $wire")
                }
            }
            if (wire.index >= 1)
                return problemWires - upstream(ZOutput(wire.index - 1))
            return problemWires
        }

        private fun testGate(gate: Gate, outputIndex: Int, list: List<GateType>): List<Wire> {
            if (list.size > indexedByOutput.size) return listOf(gate.out)
            if (gate.type == GateType.XOR) {
                if (gate.inp1 is Input && gate.inp2 is Input && gate.inp1.index == outputIndex && gate.inp2.index == outputIndex && list.isEmpty())
                    return emptyList()
                if (gate.inp1 is Input && gate.inp2 is Input && gate.inp1.index == gate.inp2.index) {
                    val diff = outputIndex - gate.inp1.index
                    if (list.size == diff * 2 + 1 && list[0] == GateType.XOR && list.indices.drop(1)
                            .all { list[it] == if (it % 2 == 0) GateType.AND else GateType.OR }
                    )
                        return emptyList()
                }
            } else if (gate.type == GateType.AND) {
                if (gate.inp1 is Input && gate.inp2 is Input && gate.inp1.index == gate.inp2.index && list.isNotEmpty()) {
                    val diff = outputIndex - gate.inp1.index
                    if (list.size == diff * 2 - 1 && list[0] == GateType.XOR && list.indices.drop(1)
                            .all { list[it] == if (it % 2 == 0) GateType.AND else GateType.OR }
                    )
                        return emptyList()
                    if (list.size == diff * 2 && list[0] == GateType.XOR && list.indices.drop(1)
                            .all { list[it] == if (it % 2 == 0) GateType.AND else GateType.OR }
                    )
                        return emptyList()
                }
            }
            val inpGate1 = indexedByOutput[gate.inp1] ?: return listOf(gate.out)
            val inpGate2 = indexedByOutput[gate.inp2] ?: return listOf(gate.out)
            val testGate1 = testGate(inpGate1, outputIndex, list + gate.type)
            val testGate2 = testGate(inpGate2, outputIndex, list + gate.type)
            return testGate1 + testGate2
        }

        private fun swap(wire1: Wire, wire2: Wire): Circuit? = Circuit(inputs, gates.map {
            when (it.out) {
                wire1 -> Gate(it.inp1, it.type, it.inp2, wire2)
                wire2 -> Gate(it.inp1, it.type, it.inp2, wire1)
                else -> it
            }
        }).takeUnless { it.hasCycle(wire1) || it.hasCycle(wire2) }

        private fun hasCycle(wire: Wire, wires: Set<Wire> = emptySet()): Boolean {
            if (wire is Input) return false
            if (wire in wires) return true
            val gate = indexedByOutput[wire] ?: return true
            val newWires = wires + wire
            return hasCycle(gate.inp1, newWires) || hasCycle(gate.inp2, newWires)
        }

        private fun upstream(w: Wire): Set<Wire> {
            if (w is Input) return setOf(w)
            val gate = checkNotNull(indexedByOutput[w]) { "No output for $w" }
            return upstream(gate.inp1) + upstream(gate.inp2) + w
        }

        private val depths = mutableMapOf<Wire, Int>()
        fun depth(w: Wire): Int = depths.getOrPut(w) {
            if (w is Input)
                w.index
            else {
                val gate = indexedByOutput[w]!!
                1 + maxOf(depth(gate.inp1), depth(gate.inp2))
            }
        }

        fun part2(knownSwaps: List<Wire> = emptyList()) {
            val problems = (0..(lastInput+1)).filter { !isValid(it) }
//            println(problems)
            if (problems.isEmpty()) {
                val solution = knownSwaps.groupingBy { it }.eachCount()
                    .filter { it.value % 2 == 1 }.keys.sortedBy { it.toString() }
                println("Solution: $knownSwaps / $solution")
                return
            }
            if (knownSwaps.size >= 8) return
            val toFix = problems.first()
            println("Lets fix $toFix")
            val optionsA = inputs.keys.asSequence().filter { it.index == toFix }.map { outputsReachableFrom(it) }
                .flatMapTo(mutableSetOf()) { it.keys }.filter { it !is Input }.filter { it !in knownSwaps }.toList()
//            println(optionsA)
            val optionsB = gates.map { it.out }.filter { it !in knownSwaps }
//            println(optionsB)
            val swaps = optionsA.flatMap { oA ->
                optionsB.filter { oB ->
//                    println("oA: $oA with oB: $oB")
                    oA != oB && swap(oA, oB).let { it != null && it.isValid(toFix) }
                }.map { oA to it }
            }
            swaps.forEach { (oA, oB) ->
//                println("Swapping $oA, $oB for $knownSwaps")
                val swap = swap(oA, oB)
                swap?.part2(knownSwaps + oA + oB)
            }
        }

        private fun outputsReachableFrom(startingWire: Wire): Map<Wire, Int> {
            val candidates = mutableMapOf(startingWire to 0)
            val result = mutableMapOf<Wire, Int>()
            while (candidates.isNotEmpty()) {
                val (wire, depth) = candidates.entries.removeFirst()
                val oldValue = result[wire]
                if (oldValue == null || depth < oldValue) {
                    result[wire] = depth
                    indexedByInput.getOrDefault(wire, emptyList()).forEach {
                        candidates[it.out] = depth + 1
                    }
                }
            }
            return result
        }

        private fun isValid(maxIndex: Int): Boolean {
            val power2doubled = copy(inputs = inputs.keys.associateWith { it.index == maxIndex }).output()
            if (power2doubled.any { (out, value) -> out.index <= maxIndex && value })
                return false
            val inputsMinus1 = inputs.keys.associateWith { it.index < maxIndex }
            val power2minus1doubled = copy(inputs = inputsMinus1).output()
            if (power2minus1doubled.any { (out, value) -> out.index <= maxIndex && value != (out.index != 0) })
                return false
            if (maxIndex > 0) {
                val inputsXplus1 =
                    inputs.keys.associateWith { if (it.prefix == "x") it.index == 0 else it.index < maxIndex }
                val xPlus1 = copy(inputs = inputsXplus1).output()
                if (xPlus1.any { (out, value) -> out.index <= maxIndex && value != (out.index == maxIndex) })
                    return false
            }
            return true
        }

        fun xpart2(swapped: List<Wire> = emptyList(), lastProblem: Int = 0) {
            val problem: Pair<Int, List<Wire>> = findProblem()
            println("$swapped / $lastProblem / $problem")
            if (problem.second.isEmpty()) {
                println("SOLUTION: $swapped")
                return
            }
            if (problem.second.any { it in swapped }) return
            if (swapped.size >= 8) return
            val (problemIndex, problemWires1) = problem
            if (problemIndex < lastProblem) return
            val attemptedWires = problemWires1.filter { it !in swapped }.sortedBy { depth(it) }
//            println("$swapped / $lastProblem / $attemptedWires")
            attemptedWires.forEach { problemWire1 ->
                val solutions = indexedByOutput.keys
                    .mapNotNull { problemWire2 ->
                        val newCircuit = swap(problemWire1, problemWire2)
                        if (newCircuit == null)
                            null
                        else
                            problemWire2 to newCircuit.findProblem()
                    }
                val attemptedSolutions = solutions.filter { (wire, problems) ->
                    (problems.first > problemIndex || (problems.first == problemIndex && (problems.second - problemWire1).size < problemWires1.size))
                }.sortedBy { -it.second.first }.map { it.first }.distinct()
//                println("$problemWire1 -> $attemptedSolutions")
                attemptedSolutions.forEach { swapWith ->
                    swap(problemWire1, swapWith)?.xpart2(swapped + listOf(problemWire1, swapWith), problemIndex)
                }
            }
        }

        private val lastInput = inputs.keys.maxOf { it.index }
        private fun findProblem(): Pair<Int, List<Wire>> =
            (0..lastInput).map { input ->
                testOutput(ZOutput(input)).let { input to it }
            }.firstOrNull() { it.second.isNotEmpty() } ?: (lastInput to emptyList())

        private fun Circuit.output() = outputGates.associateWith { valueFor(it) }
    }

    fun part1(input: String): Long {
        val puzzle = parse(input)
        return puzzle.part1()
    }

    private fun parse(input: String): Circuit {
        val (sInputs, sGates) = input.paragraphs()
        val inputs =
            sInputs.lines().map { it.split(": ") }.associate { (name, s01) -> Input(name) to (s01.toInt() == 1) }
        val gates = sGates.lines().map { it.split(" ") }.map { (inp1, type, inp2, _, out) ->
            Gate(Wire(inp1), GateType.valueOf(type), Wire(inp2), Wire(out))
        }
        return Circuit(inputs, gates)
    }

    fun part2(input: String): String {
        val puzzle = parse(input)
        puzzle.part2()
        return ""
    }
}
