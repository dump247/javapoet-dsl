package com.mostlycertain.javapoetdsl

import com.mostlycertain.javapoetdsl.Format.normalizeFormat
import com.mostlycertain.javapoetdsl.Format.transformArgs
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.TypeName
import kotlin.reflect.KClass

/**
 * Expression defined with Java code.
 *
 * @see [logicalOr]
 * @see [logicalAnd]
 * @see [expression]
 */
abstract class CodeExpression : Format.JavaCodeBlock {
    open val isEmpty: Boolean get() = toCodeBlock().isEmpty
    val isNotEmpty: Boolean get() = !isEmpty

    override fun equals(other: Any?): Boolean = when {
        other === this -> true
        other !is CodeExpression -> false
        else -> toCodeBlock().equals(other.toCodeBlock())
    }

    override fun hashCode(): Int {
        return toCodeBlock().hashCode()
    }

    override fun toString(): String {
        return toCodeBlock().toString()
    }
}

/**
 * Expression wrapper around a single [CodeBlock].
 */
internal data class CodeBlockExpression(private val code: CodeBlock) : CodeExpression() {
    override fun toCodeBlock() = code
}

private object EmptyExpression : CodeExpression() {
    val EMPTY_BLOCK: CodeBlock = CodeBlock.of("")

    override fun toCodeBlock() = EMPTY_BLOCK
}

/**
 * Generate a method invocation expression.
 *
 * Example:
 *   methodInvoke("this.method", literal(100))
 *   // output: this.method(100)
 *
 * @param context method being invoked
 * @param parameters expressions that evaluate to the parameter values passed to the constructor
 */
fun methodInvoke(context: CodeExpression, parameters: List<CodeExpression> = listOf()): CodeExpression {
    val builder = CodeBlock.builder()
    builder.add(context.toCodeBlock())
    builder.add("(").indent()

    // Putting expressions on separate lines at 3 parameters is arbitrary. Ideally, this would break once the
    // length of the line hits a limit, but that is more complicated.
    val parameterSeparator = if (parameters.size > 3) {
        builder.add("\n")
        ",\n"
    } else {
        ", "
    }

    parameters.forEachIndexed { index, param ->
        if (index > 0) {
            builder.add(parameterSeparator)
        }

        builder.add(param.toCodeBlock())
    }

    builder.unindent().add(")")
    return builder.buildExpression()
}

fun methodInvoke(context: CodeExpression, vararg parameters: CodeExpression) = methodInvoke(context, parameters.toList())

fun methodInvoke(context: String, vararg parameters: CodeExpression) = methodInvoke(e(context), parameters.toList())

fun methodInvoke(context: String, parameters: List<CodeExpression> = listOf()) = methodInvoke(e(context), parameters)

/**
 * Generate a constructor invocation expression.
 *
 * Example:
 *   constructorInvoke(StringBuilder::class, literal(100))
 *   // output: new StringBuilder(100)
 *
 * @param type type to instantiate
 * @param parameters expressions that evaluate to the parameter values passed to the constructor
 */
fun constructorInvoke(type: TypeName, parameters: List<CodeExpression> = listOf()): CodeExpression {
    return methodInvoke(e("new %T", type), parameters)
}

/**
 * Generate a constructor invocation expression.
 *
 * Example:
 *   constructorInvoke(StringBuilder::class, literal(100))
 *   // output: new StringBuilder(100)
 *
 * @param type type to instantiate
 * @param parameters expressions that evaluate to the parameter values passed to the constructor
 */
fun constructorInvoke(type: TypeName, vararg parameters: CodeExpression) = constructorInvoke(type, parameters.toList())

/**
 * Generate a constructor invocation expression.
 *
 * Example:
 *   constructorInvoke(StringBuilder::class, literal(100))
 *   // output: new StringBuilder(100)
 *
 * @param type type to instantiate
 * @param parameters expressions that evaluate to the parameter values passed to the constructor
 */
fun constructorInvoke(type: KClass<*>, vararg parameters: CodeExpression) = constructorInvoke(TypeNames.of(type), parameters.toList())

/**
 * Generate a constructor invocation expression.
 *
 * Example:
 *   constructorInvoke(StringBuilder::class, literal(100))
 *   // output: new StringBuilder(100)
 *
 * @param type type to instantiate
 * @param parameters expressions that evaluate to the parameter values passed to the constructor
 */
fun constructorInvoke(type: KClass<*>, parameters: List<CodeExpression> = listOf()) = constructorInvoke(TypeNames.of(type), parameters)

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
private data class LogicalOrExpression(val expressions: List<CodeExpression>) : CodeExpression() {
    init {
        check(expressions.size > 1)
    }

    companion object {
        private val SHORT_OP = CodeBlock.of(" || ")
        private val LONG_OP = CodeBlock.of("\n|| ")
    }

    override val isEmpty = false
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
private data class LogicalAndExpression(val expressions: List<CodeExpression>) : CodeExpression() {
    init {
        check(expressions.size > 1)
    }

    companion object {
        private val SHORT_OP = CodeBlock.of(" && ")
        private val LONG_OP = CodeBlock.of("\n&& ")
    }

    override val isEmpty = false
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
private data class BinaryOrExpression(val expressions: List<CodeExpression>) : CodeExpression() {
    init {
        check(expressions.size > 1)
    }

    companion object {
        private val SHORT_OP = CodeBlock.of(" | ")
        private val LONG_OP = CodeBlock.of("\n| ")
    }

    override val isEmpty = false
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
 * @param expressions expressions to join
 * @throws IllegalArgumentException if [expressions] is empty
 */
fun logicalAnd(expressions: List<CodeExpression>): CodeExpression = combineExpressions(::LogicalAndExpression, expressions)

/**
 * Join a series of expressions with a logical "and" operator (&&).
 *
 * @param expressions expressions to join
 * @throws IllegalArgumentException if [expressions] is empty
 */
fun logicalAnd(vararg expressions: CodeExpression): CodeExpression = combineExpressions(::LogicalAndExpression, expressions.toList())

/**
 * Join a series of expressions with a logical "or" operator (||).
 *
 * @param expressions expressions to join
 * @throws IllegalArgumentException if [expressions] is empty
 */
fun logicalOr(expressions: List<CodeExpression>): CodeExpression = combineExpressions(::LogicalOrExpression, expressions)

/**
 * Join a series of expressions with a logical "or" operator (||).
 *
 * @param expressions expressions to join
 * @throws IllegalArgumentException if [expressions] is empty
 */
fun logicalOr(vararg expressions: CodeExpression): CodeExpression = combineExpressions(::LogicalOrExpression, expressions.toList())

/**
 * Join a series of expressions with a binary "or" operator (|).
 *
 * @param expressions expressions to join
 * @throws IllegalArgumentException if [expressions] is empty
 */
fun binaryOr(expressions: List<CodeExpression>): CodeExpression = combineExpressions(::BinaryOrExpression, expressions)

/**
 * Join a series of expressions with a binary "or" operator (|).
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

class ExpressionBuilder(private val code: CodeBlock.Builder) {
    /**
     * Add some code snippet to the expression.
     */
    fun write(format: String, vararg args: Any) {
        write(expression(format, *args))
    }

    /**
     * Add some code snippet to the expression.
     */
    fun write(expression: CodeExpression) {
        code.add(expression.toCodeBlock())
    }

    /**
     * Add some code snippet to the expression followed by a line break.
     */
    fun writeln(format: String, vararg args: Any) = writeln(expression(format, *args))

    /**
     * Add some code snippet to the expression followed by a line break.
     */
    fun writeln(expression: CodeExpression) {
        write(expression)
        code.add("\n")
    }

    /**
     * Add a blank line.
     */
    fun writeln() {
        code.add("\n")
    }

    /**
     * Increase the indent level of the expression.
     */
    fun indent(block: ExpressionBuilder.() -> Unit) {
        try {
            code.indent()
            this.block()
        } finally {
            code.unindent()
        }
    }
}

/**
 * Build a java code expression.
 *
 * Example of calling a builder:
 *
 * expression {
 *   writeln("%T.builder()", BuilderClass);
 *
 *   indent {
 *     writeln(".foo(%L)", fooValue)
 *     writeln(".bar(%L)", barValue)
 *     write(".build()")
 *   }
 * }
 */
fun expression(block: ExpressionBuilder.() -> Unit): CodeExpression {
    val codeBlock = CodeBlock.builder()
    val builder = ExpressionBuilder(codeBlock)
    builder.block()

    if (codeBlock.isEmpty) {
        return emptyExpression()
    }

    return codeBlock.buildExpression()
}

/**
 * Build an expression where all the lines after the first are indented.
 *
 * Example:
 *
 * expression(
 *   e("%T.builder()", BuilderClass),
 *   e(".foo(%L)", fooValue),
 *   e(".bar(%L)", barValue),
 *   e(".build()"),
 * )
 */
fun expression(vararg lines: CodeExpression): CodeExpression = expression(lines.toList())

/**
 * Build an expression where all the lines after the first are indented.
 *
 * Example:
 *
 * expression(
 *   e("%T.builder()", BuilderClass),
 *   e(".foo(%L)", fooValue),
 *   e(".bar(%L)", barValue),
 *   e(".build()"),
 * )
 */
fun expression(lines: List<CodeExpression>): CodeExpression {
    if (lines.isEmpty()) {
        return emptyExpression()
    } else if (lines.size == 1) {
        return lines[0]
    }

    return expression {
        write(lines[0])

        indent { lines.drop(1).forEach { writeln(); write(it) } }
    }
}

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

/**
 * Render a lambda function.
 *
 * The lambda is always rendered with parens around the parameters. The result looks like this:
 *
 * ```java
 * (<parameterNames>) -> {
 *     <block>
 * }
 * ```
 *
 * NOTE: A line break is NOT rendered after the closing curly brace.
 *
 * @param parameterNames Ordered list of parameter names for the lambda.
 * @param block Function to render the body of the lambda.
 */
fun lambda(vararg parameterNames: String, block: CodeFunc) = lambda(parameterNames.toList(), block)

/**
 * Render a lambda function.
 *
 * The lambda is always rendered with parens around the parameters. The result looks like this:
 *
 * ```java
 * (<parameterNames>) -> {
 *     <block>
 * }
 * ```
 *
 * NOTE: A line break is NOT rendered after the closing curly brace.
 *
 * @param parameterNames Ordered list of parameter names for the lambda.
 * @param block Function to render the body of the lambda.
 */
fun lambda(parameterNames: List<String>, block: CodeFunc): CodeExpression {
    return codeBlock() {
        lambdaDecl(parameterNames, block)
    }.toExpression()
}

/**
 * Render a lambda function with a single expression and no curly braces.
 *
 * The lambda is always rendered with parens around the parameters. The result looks like this:
 *
 * ```java
 * (<parameterNames>) -> <expression>
 * ```
 *
 * NOTE: A line break is NOT rendered after the expression.
 *
 * @param parameterNames Ordered list of parameter names for the lambda.
 * @param expression Expression that produces the lambda result.
 */
fun lambda(parameterNames: List<String>, expression: CodeExpression): CodeExpression {
    return codeBlock() {
        lambdaDecl(parameterNames, expression)
    }.toExpression()
}

/**
 * Render a lambda function with a single expression and no curly braces.
 *
 * The lambda is always rendered with parens around the parameters. The result looks like this:
 *
 * ```java
 * (<parameterNames>) -> <expression>
 * ```
 *
 * NOTE: A line break is NOT rendered after the expression.
 *
 * @param parameterNames Ordered list of parameter names for the lambda.
 * @param expression Expression that produces the lambda result.
 */
fun lambda(vararg parameterNames: String, expression: CodeExpression) = lambda(parameterNames.toList(), expression)

/**
 * Render a lambda function with a single expression and no curly braces or parameters.
 *
 * The result looks like this:
 *
 * ```java
 * () -> <expression>
 * ```
 *
 * NOTE: A line break is NOT rendered after the expression.
 *
 * @param expression Expression that produces the lambda result.
 */
fun lambda(expression: CodeExpression): CodeExpression {
    return codeBlock() {
        lambdaDecl(expression)
    }.toExpression()
}