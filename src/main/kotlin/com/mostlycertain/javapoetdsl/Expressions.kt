package com.mostlycertain.javapoetdsl

import com.mostlycertain.javapoetdsl.Format.normalizeFormat
import com.mostlycertain.javapoetdsl.Format.transformArgs
import com.squareup.javapoet.CodeBlock

/**
 * Expression defined with Java code.
 *
 * @see [logicalOr]
 * @see [logicalAnd]
 * @see [expression]
 */
interface CodeExpression : Format.JavaCodeBlock

/**
 * Expression wrapper around a single [CodeBlock].
 */
private data class CodeBlockExpression(private val code: CodeBlock) : CodeExpression {
    override fun toCodeBlock() = code
}

private object EmptyExpression : CodeExpression {
    val EMPTY_BLOCK: CodeBlock = CodeBlock.of("")

    override fun toCodeBlock() = EMPTY_BLOCK
}

/**
 * @param expressions expressions to join
 * @param shortOp operator to emit when the number of expressions is below the threshold for a line break
 *                  (should not contain a line break)
 * @param longOp operator to emit when the number of expressions reaches the threshold for a line break
 *                  (should start with a line break)
 */
private fun joinExpressions(
        expressions: Sequence<CodeExpression>,
        shortOp: CodeBlock,
        longOp: CodeBlock
): CodeBlock {
    val expressionsList = expressions.toList()
    val code = CodeBlock.builder()

    // Putting expressions on separate lines at 3 expressions is arbitrary. Ideally, this would break once the
    // length of the line hits a limit, but that is more complicated.
    val separator = if (expressionsList.size < 3) shortOp else longOp

    expressionsList.forEachIndexed { index, expression ->
        if (index > 0) {
            code.add(separator)
        }

        when (expression) {
            is CodeBlockExpression -> code.add(expression.toCodeBlock())
            else -> code.add("(\$L)", expression.toCodeBlock())
        }
    }

    return code.build()
}

/**
 * Series of expressions joined with a logical "or" operator (||).
 */
private data class LogicalOrExpression(val expressions: List<CodeExpression>) : CodeExpression {
    init {
        check(expressions.size > 1)
    }

    companion object {
        private val SHORT_OP = CodeBlock.of(" || ")
        private val LONG_OP = CodeBlock.of("\n|| ")
    }

    override fun toCodeBlock(): CodeBlock = joinExpressions(this.flatten(), SHORT_OP, LONG_OP)

    fun flatten(): Sequence<CodeExpression> {
        return expressions.asSequence().flatMap {
            when (it) {
                is LogicalOrExpression -> it.flatten()
                else -> sequenceOf(it)
            }
        }
    }
}

/**
 * Series of expressions joined with a logical "and" operator (&&).
 *
 * @see [logicalAnd]
 */
private data class LogicalAndExpression(val expressions: List<CodeExpression>) : CodeExpression {
    init {
        check(expressions.size > 1)
    }

    companion object {
        private val SHORT_OP = CodeBlock.of(" && ")
        private val LONG_OP = CodeBlock.of("\n&& ")
    }

    override fun toCodeBlock(): CodeBlock = joinExpressions(this.flatten(), SHORT_OP, LONG_OP)

    fun flatten(): Sequence<CodeExpression> {
        return expressions.asSequence().flatMap {
            when (it) {
                is LogicalAndExpression -> it.flatten()
                else -> sequenceOf(it)
            }
        }
    }
}

/**
 * Series of expressions joined with a binary "or" operator (|).
 *
 * @see [binaryOr]
 */
private data class BinaryOrExpression(val expressions: List<CodeExpression>) : CodeExpression {
    init {
        check(expressions.size > 1)
    }

    companion object {
        private val SHORT_OP = CodeBlock.of(" | ")
        private val LONG_OP = CodeBlock.of("\n| ")
    }

    override fun toCodeBlock(): CodeBlock = joinExpressions(this.flatten(), SHORT_OP, LONG_OP)

    fun flatten(): Sequence<CodeExpression> {
        return expressions.asSequence().flatMap {
            when (it) {
                is BinaryOrExpression -> it.flatten()
                else -> sequenceOf(it)
            }
        }
    }
}

private fun combineExpressions(
        cons: (List<CodeExpression>) -> CodeExpression,
        expressions: List<CodeExpression>
): CodeExpression {
    return when {
        expressions.isEmpty() -> throw IllegalArgumentException("expressions is empty or all null")
        expressions.size == 1 -> expressions[0]
        else -> cons(expressions)
    }
}

/**
 * Join a series of expressions with a logical "and" operator (&&).
 *
 * Null values in [expressions] are ignored.
 *
 * @param expressions expressions to join
 * @throws IllegalArgumentException if [expressions] is empty
 */
fun logicalAnd(expressions: List<CodeExpression>): CodeExpression = combineExpressions(::LogicalAndExpression, expressions)

/**
 * Join a series of expressions with a logical "and" operator (&&).
 *
 * Null values in [expressions] are ignored.
 *
 * @param expressions expressions to join
 * @throws IllegalArgumentException if [expressions] is empty
 */
fun logicalAnd(vararg expressions: CodeExpression): CodeExpression = combineExpressions(::LogicalAndExpression, expressions.toList())

/**
 * Join a series of expressions with a logical "or" operator (||).
 *
 * Null values in [expressions] are ignored.
 *
 * @param expressions expressions to join
 * @throws IllegalArgumentException if [expressions] is empty
 */
fun logicalOr(expressions: List<CodeExpression>): CodeExpression = combineExpressions(::LogicalOrExpression, expressions)

/**
 * Join a series of expressions with a logical "or" operator (||).
 *
 * Null values in [expressions] are ignored.
 *
 * @param expressions expressions to join
 * @throws IllegalArgumentException if [expressions] is empty
 */
fun logicalOr(vararg expressions: CodeExpression): CodeExpression = combineExpressions(::LogicalOrExpression, expressions.toList())

/**
 * Join a series of expressions with a binary "or" operator (|).
 *
 * Null values in [expressions] are ignored.
 *
 * @param expressions expressions to join
 * @throws IllegalArgumentException if [expressions] is empty
 */
fun binaryOr(expressions: List<CodeExpression>): CodeExpression = combineExpressions(::BinaryOrExpression, expressions)

/**
 * Join a series of expressions with a binary "or" operator (|).
 *
 * Null values in [expressions] are ignored.
 *
 * @param expressions expressions to join
 * @throws IllegalArgumentException if [expressions] is empty
 */
fun binaryOr(vararg expressions: CodeExpression): CodeExpression = combineExpressions(::BinaryOrExpression, expressions.toList())

/**
 * Format java code as an expression.
 *
 * The format markers are the sames as javapoet, except that this expects percent signs (%) as the format
 * indicator, rather than dollar signs ($).
 *
 * @param format code format string
 * @param args format string arguments
 *
 * @see [e]
 */
fun expression(format: String, vararg args: Any): CodeExpression {
    if (format.isEmpty() && args.isEmpty()) {
        return emptyExpression()
    }

    return CodeBlockExpression(CodeBlock.of(normalizeFormat(format), *transformArgs(args)))
}

/**
 * Short alias for [expression].
 */
fun e(format: String, vararg args: Any) = expression(format, *args)

fun emptyExpression(): CodeExpression = EmptyExpression

fun literalNull() = expression("null")

fun literal(value: String) = expression("%S", value)

fun literal(value: Char) = when (value) {
    '\n' -> expression("'\\n'")
    '\r' -> expression("'\\r'")
    '\'' -> expression("'\\''")
    else -> expression("'%L'", value)
}

fun literal(value: Byte) = expression("%L", value)

fun literal(value: Short) = expression("%L", value)

fun literal(value: Int) = expression("%L", value)

fun literal(value: Long) = expression("%LL", value)

fun literal(value: Float) = when {
    value.isNaN() -> expression("%T.NaN", java.lang.Float::class)
    value == Float.POSITIVE_INFINITY -> expression("%T.POSITIVE_INFINITY", java.lang.Float::class)
    value == Float.NEGATIVE_INFINITY -> expression("%T.NEGATIVE_INFINITY", java.lang.Float::class)
    else -> expression("%L", value)
}

fun literal(value: Double) = when {
    value.isNaN() -> expression("%T.NaN", java.lang.Double::class)
    value == Double.POSITIVE_INFINITY -> expression("%T.POSITIVE_INFINITY", java.lang.Double::class)
    value == Double.NEGATIVE_INFINITY -> expression("%T.NEGATIVE_INFINITY", java.lang.Double::class)
    else -> expression("%LD", value)
}

fun literal(value: Boolean) = when (value) {
    true -> expression("true")
    false -> expression("false")
}