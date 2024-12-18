package utils

import kotlin.math.absoluteValue

interface Solution {
    operator fun get(name: String): Expression.Constant
}

interface ProblemBuilder<E> {
    fun variable(name: String): E
    operator fun Int.plus(term: E): E
    operator fun Int.minus(term: E): E
    operator fun Int.times(factor: E): E
    operator fun Int.div(divisor: E): E
    operator fun Long.plus(term: E): E
    operator fun Long.minus(term: E): E
    operator fun Long.times(factor: E): E
    operator fun Long.div(divisor: E): E
    operator fun E.plus(term: Int): E
    operator fun E.minus(term: Int): E
    operator fun E.times(factor: Int): E
    operator fun E.div(factor: Int): E
    operator fun E.plus(term: Long): E
    operator fun E.minus(term: Long): E
    operator fun E.times(factor: Long): E
    operator fun E.div(factor: Long): E
    operator fun E.plus(term: E): E
    operator fun E.minus(term: E): E
    operator fun E.times(factor: E): E
    operator fun E.div(divisor: E): E
    operator fun E.unaryMinus(): E
    infix fun E.eq(c: Int)
    infix fun E.eq(c: Long)
    infix fun E.eq(o: E)
}

fun solve(builder: ProblemBuilder<Expression>.() -> Unit): Solution =
    SimpleEquations().apply(builder)

private typealias ValueHandler = (Expression.Constant) -> Unit

sealed interface Expression {
    fun variables(): Set<Variable>
    data class Constant(val numerator: Long, val denominator: Long) : Expression {
        constructor(value: Int) : this(value.toLong(), 1L)
        constructor(value: Long) : this(value, 1L)

        override fun variables(): Set<Variable> = emptySet()

        operator fun plus(o: Constant): Constant =
            Constant(numerator * o.denominator + o.numerator * denominator, denominator * o.denominator).simplify()

        operator fun minus(o: Constant): Constant =
            Constant(numerator * o.denominator - o.numerator * denominator, denominator * o.denominator).simplify()

        operator fun times(o: Constant): Constant =
            Constant(numerator * o.numerator, denominator * o.denominator).simplify()

        operator fun div(o: Constant): Constant =
            Constant(numerator * o.denominator, denominator * o.numerator).simplify()

        private fun simplify(): Constant {
            val gcd = greatestCommonDivisor(numerator.absoluteValue, denominator.absoluteValue)
            return if (denominator < 0)
                Constant(-numerator / gcd, -denominator / gcd)
            else if (gcd == 1L)
                this
            else
                Constant(numerator / gcd, denominator / gcd)
        }

        fun toInt(): Int {
            check(denominator == 1L)
            return numerator.toInt()
        }

        fun asLong(): Long? = if (denominator != 1L) null else numerator

        fun toLong(): Long {
            check(denominator == 1L) { "no long value for $numerator / $denominator" }
            return numerator
        }

        override fun toString(): String = if (denominator == 1L) numerator.toString() else
            "$numerator / $denominator"
    }

    class Variable(private val name: String) : Expression {
        var knownValue = false
        private var value: Constant? = null
        private val listeners = mutableListOf<ValueHandler>()
        fun ifResolved(handler: ValueHandler) {
            val v = value
            if (v == null)
                listeners.add(handler)
            else
                handler(v)
        }

        fun resolved(v: Constant) {
            this.knownValue = true
            this.value = v
            while (listeners.isNotEmpty())
                listeners.removeFirst()(v)
        }

        fun requireValue() = requireNotNull(value) { "Value for $name not known yet" }
        override fun variables(): Set<Variable> = if (knownValue) emptySet() else setOf(this)
        override fun toString(): String = "var[$name = $value]"
    }

    data class Add(val terms: List<Expression>) : Expression {
        constructor(term1: Expression, term2: Expression) : this(listOf(term1, term2))

        operator fun plus(term: Expression) = Add(terms + term)
        override fun variables(): Set<Variable> = terms.flatMapTo(mutableSetOf()) { it.variables() }
        override fun toString(): String = terms.joinToString(" + ", "(", ")")
    }

    data class Multiply(val factor1: Expression, val factor2: Expression) : Expression {
        override fun variables(): Set<Variable> = factor1.variables() + factor2.variables()
        override fun toString(): String = "($factor1 * $factor2)"
    }

    data class Divide(val dividend: Expression, val divisor: Expression) : Expression {
        override fun variables(): Set<Variable> = dividend.variables() + divisor.variables()
        override fun toString(): String = "($dividend / $divisor)"
    }
}

private fun List<Expression.Constant>.sum(): Expression.Constant =
    fold(cZero) { acc, c -> acc + c }

val cZero = Expression.Constant(0)
val cOne = Expression.Constant(1)

private class SimpleEquations : ProblemBuilder<Expression>, Solution {
    data class Equation(val left: Expression, val right: Expression) {
        fun isIdentity(): Boolean = left == right || (left.variables().isEmpty() && right.variables().isEmpty())
    }

    private val knownVariables = mutableMapOf<String, Expression.Variable>()
    private val equations = mutableListOf<Equation>()

    override operator fun get(name: String): Expression.Constant {
        solve()
        val variable = knownVariables.getValue(name)
        if (variable.knownValue)
            return requireNotNull(variable.requireValue())
        TODO("solve using $equations")
    }

    private fun solve() {
        val grouped = equations.groupBy { it.left.variables() + it.right.variables() }
        if (equations.isEmpty()) return
        val singles = grouped.filterKeys { it.size == 1 }
        if (singles.isNotEmpty()) {
            singles.forEach { (variables, equations) ->
                val variable = variables.single()
                Simplifier(variable).simplify(equations.first())
                check(variable.knownValue) { "Expected value but didn't solve $equations" }
            }
            solve()
        } else {
            val solveable = grouped.entries.firstOrNull { (variables, equations) -> variables.size <= equations.size }
            if (solveable == null)
                TODO("solve for $grouped")
            else {
                val target = solveable.key.first()
                val targetExtracted = solveable.value.map {
                    Simplifier(target).simplify(it)
                }
                val (t1, t2) = targetExtracted
                this.equations.add(Equation(t1, t2))
                solve()
            }
        }
    }

    inner class Simplifier(private val target: Expression.Variable) {
        fun simplify(eq: Equation) = simplify(simplify(eq.left), simplify(eq.right))

        fun simplify(e: Expression): Expression {
            if (e is Expression.Multiply) {
                if (e.factor1 is Expression.Add)
                    return simplify(Expression.Add(e.factor1.terms.map { it * e.factor2 }))
                if (e.factor2 is Expression.Add)
                    return simplify(Expression.Add(e.factor2.terms.map { it * e.factor1 }))
                if (e.factor2 is Expression.Constant)
                    return simplify(e.factor2 * e.factor1)
                if (e.factor1 is Expression.Constant && e.factor2 is Expression.Multiply)
                    return simplify((e.factor1 * e.factor2.factor1) * e.factor2.factor2)
            }
            if (e is Expression.Add) {
                val constants = e.terms.filterIsInstance<Expression.Constant>()
                if (constants.size >= 2)
                    return simplify(e.removeAll(constants) + constants.sum())
                if (constants.size == 1 && constants.single() == cZero)
                    return simplify(e.removeAll(constants))
                val variables = e.terms.filterIsInstance<Expression.Variable>()
                val groupedVariables = variables.groupingBy { it }.eachCount()
                if (groupedVariables.any { it.value > 1 })
                    return simplify(groupedVariables.entries.fold(e.removeAll(variables)) { acc, (variable, count) ->
                        acc + (count * variable)
                    })
                val flattenedTerms = e.terms.flatMap { if (it is Expression.Add) it.terms else listOf(it) }
                if (flattenedTerms.size != e.terms.size)
                    return simplify(sum(flattenedTerms))
                val simplifiedTerms = e.terms.associateWith { simplify(it) }
                if (simplifiedTerms.any { it.key != it.value })
                    return simplify(sum(simplifiedTerms.values.toList()))
            }
            if (e is Expression.Divide) {
                val sDividend = simplify(e.dividend)
                val sDivisor = simplify(e.divisor)
                if (sDividend != e.dividend || sDivisor != e.divisor)
                    return sDividend / sDivisor
                if (e.divisor is Expression.Constant)
                    return (cOne / e.divisor) * e.dividend
            }
            return e
        }

        fun simplify(left: Expression, right: Expression): Expression {
            if (target in right.variables())
                return if (target in left.variables())
                    simplify(simplify(left - right), value(0))
                else
                    simplify(right, left)
            check(target in left.variables())
            when {
                right is Expression.Constant && left.variables() == setOf(target) ->
                    return simplify(left, right)

                left is Expression.Multiply -> {
                    val f1 = target in left.factor1.variables()
                    val f2 = target in left.factor2.variables()
                    if (!f1 && f2)
                        return simplify(left.factor2, right / left.factor1)

                }

                left is Expression.Add -> {
                    val (withTarget, withoutTarget) = left.terms.partition { target in it.variables() }
                    return simplify(simplify(sum(withTarget)), simplify(right - sum(withoutTarget)))
                }

                left == target -> return right
            }
            TODO("simplify using ($left = $right")
        }

        private fun Expression.isMultipleOfTarget(): Boolean = when (this) {
            is Expression.Variable -> this == target
            is Expression.Constant -> false
            is Expression.Add -> terms.all { it.isMultipleOfTarget() }
            is Expression.Multiply -> factor1.isMultipleOfTarget() || factor2.isMultipleOfTarget()
            is Expression.Divide -> dividend.isMultipleOfTarget() && target !in divisor.variables()
        }

        private fun Expression.divideByTarget(): Expression = when (this) {
            is Expression.Variable -> value(1)
            is Expression.Constant -> error("Cannot simplify $this by dividing by $target")
            is Expression.Add -> sum(terms.map { it.divideByTarget() })
            is Expression.Multiply -> when {
                factor1.isMultipleOfTarget() -> factor1.divideByTarget() * factor2
                factor2.isMultipleOfTarget() -> factor1 * factor2.divideByTarget()
                else -> error("Cannot simplify $this by dividing by $target")
            }

            is Expression.Divide -> {
                check(target !in divisor.variables()) { "Cannot simplify $this by dividing by $target" }
                dividend.divideByTarget() / divisor
            }
        }


        fun Expression.Add.removeAll(termsToRemove: Iterable<Expression>): Expression {
            check(termsToRemove.all { it in terms }) { "$termsToRemove should all be in $terms" }
            return sum(terms - termsToRemove.toSet())
        }

        private fun sum(terms: List<Expression>): Expression = when {
            terms.isEmpty() -> value(0)
            terms.size == 1 -> terms.single()
            else -> Expression.Add(terms)
        }

        fun simplify(left: Expression, right: Expression.Constant): Expression {
            when (left) {
                is Expression.Add -> {
                    val constants = left.terms.filterIsInstance<Expression.Constant>()
                    if (constants.isNotEmpty())
                        return simplify(left.removeAll(constants), right - constants.sum())
                }

                is Expression.Multiply ->
                    if (left.factor1 is Expression.Constant)
                        return simplify(left.factor2, right / left.factor1)

                is Expression.Divide ->
                    if (left.divisor is Expression.Constant)
                        return simplify(left.dividend, right * left.divisor)
                    else if (left.dividend is Expression.Constant)
                        return simplify(left.divisor, left.dividend / right)

                is Expression.Constant -> error("No variable found!")
                is Expression.Variable -> {
                    left.resolved(right)
                    return right
                }
            }
            if (left.isMultipleOfTarget()) {
                val divideByTarget = left.divideByTarget()
                return simplify(target, simplify(right / divideByTarget))
            }
            TODO("simplify $left = [C] $right")
        }
    }

    override fun variable(name: String): Expression =
        knownVariables.getOrPut(name) {
            Expression.Variable(name).apply {
                ifResolved { updateEquations() }
            }
        }

    private fun updateEquations() {
        equations.replaceAll { updateEquation(it) }
        equations.removeIf { it.isIdentity() }
    }

    private fun updateEquation(e: Equation): Equation =
        Equation(e.left.resolveValues(), e.right.resolveValues())

    private fun Expression.resolveValues(): Expression = when (this) {
        is Expression.Variable -> if (knownValue) requireValue() else this
        is Expression.Constant -> this
        is Expression.Add -> Expression.Add(terms.map { it.resolveValues() })
        is Expression.Multiply -> factor1.resolveValues() * factor2.resolveValues()
        is Expression.Divide -> dividend.resolveValues() / divisor.resolveValues()
    }

    private fun value(value: Int) = Expression.Constant(value)
    private fun value(value: Long) = Expression.Constant(value)

    override fun Int.plus(term: Expression): Expression = value(this) + term
    override fun Int.minus(term: Expression): Expression = value(this) - term
    override fun Int.times(factor: Expression): Expression = value(this) * factor
    override fun Int.div(divisor: Expression): Expression = value(this) / divisor

    override fun Long.plus(term: Expression): Expression = value(this) + term
    override fun Long.minus(term: Expression): Expression = value(this) - term
    override fun Long.times(factor: Expression): Expression = value(this) * factor
    override fun Long.div(divisor: Expression): Expression = value(this) / divisor

    override fun Expression.plus(term: Int) = this + value(term)
    override fun Expression.minus(term: Int) = this + value(-term)
    override fun Expression.times(factor: Int) = this * value(factor)
    override fun Expression.div(factor: Int) = this / value(factor)
    override fun Expression.unaryMinus() = this * value(-1)

    override fun Expression.plus(term: Long) = this + value(term)
    override fun Expression.minus(term: Long) = this + value(-term)
    override fun Expression.times(factor: Long) = this * value(factor)
    override fun Expression.div(factor: Long) = this / value(factor)

    override fun Expression.plus(term: Expression): Expression =
        if (this is Expression.Constant && term is Expression.Constant)
            this + term
        else if (this is Expression.Add)
            this + term
        else
            Expression.Add(this, term)

    override fun Expression.minus(term: Expression): Expression =
        if (this is Expression.Constant && term is Expression.Constant)
            this - term
        else
            Expression.Add(this, -term)

    override fun Expression.times(factor: Expression): Expression =
        if (this is Expression.Constant && factor is Expression.Constant)
            this * factor
        else
            Expression.Multiply(this, factor)

    override fun Expression.div(divisor: Expression): Expression =
        if (this is Expression.Constant && divisor is Expression.Constant)
            this / divisor
        else
            Expression.Divide(this, divisor)

    override fun Expression.eq(c: Int) {
        this eq value(c)
    }

    override fun Expression.eq(c: Long) {
        this eq value(c)
    }

    override fun Expression.eq(o: Expression) {
        equations.add(Equation(this, o))
    }
}
