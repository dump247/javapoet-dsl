package com.mostlycertain.javapoetdsl

import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.TypeName
import java.lang.reflect.Type
import kotlin.reflect.KClass

typealias CodeFunc = CodeBuilder.() -> Unit

internal fun buildBlock(code: CodeBlock.Builder, block: CodeFunc): CodeBlock.Builder {
    val builder = CodeBuilder(code)
    builder.block()
    builder.close()
    return code
}

/**
 * Generate a block of java code.
 *
 * @param block Closure that generates the code. The closure is invoked on a [CodeBuilder].
 * @return Generated code block.
 */
fun codeBlock(block: CodeFunc): CodeBlock {
    return buildBlock(CodeBlock.builder(), block).build()
}

open class CodeBuilder(private val code: CodeBlock.Builder) {
    private var currentFlow: Flow? = null

    internal fun close() {
        endCurrentFlow()
    }

    internal fun endCurrentFlow() {
        if (currentFlow != null) {
            currentFlow = null
            code.endControlFlow()
        }
    }

    /**
     * Increase the current indentation level.
     */
    fun indent(block: CodeFunc) {
        endCurrentFlow()

        try {
            code.indent()
            this.block()
        } finally {
            code.unindent()
        }
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
    fun lambdaDecl(vararg parameterNames: String, block: CodeFunc) {
        lambdaDecl(parameterNames.toList(), block)
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
    fun lambdaDecl(parameterNames: List<String>, block: CodeFunc) {
        write("(%L) -> {\n", parameterNames.joinToString(", "))

        indent(block)

        this.write("}")
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
    fun lambdaDecl(vararg parameterNames: String, expression: CodeExpression) {
        lambdaDecl(parameterNames.toList(), expression)
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
    fun lambdaDecl(parameterNames: List<String>, expression: CodeExpression) {
        write("(%L) -> %L", parameterNames.joinToString(", "), expression)
    }

    /**
     * Render a lambda function with a single expression and no parameters or curly braces.
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
    fun lambdaDecl(expression: CodeExpression) {
        write("() -> %L", expression)
    }

    /**
     * Write an arbitrary block of code.
     */
    fun write(format: String, vararg args: Any) = write(expression(format, *args).toCodeBlock())

    /**
     * Write an arbitrary block of code.
     */
    fun write(expression: CodeExpression) = write(expression.toCodeBlock())

    /**
     * Write an arbitrary block of code.
     */
    fun write(block: CodeBlock) {
        endCurrentFlow()
        code.add(block)
    }

    /**
     * Render a java statement.
     *
     * The provided code will have a semicolon and line break appended.
     *
     * @see [s]
     */
    fun statementDecl(format: String, vararg args: Any) = statementDecl(expression(format, *args))

    /**
     * Render a java statement.
     *
     * The provided code will have a semicolon and line break appended.
     *
     * @see [s]
     */
    fun statementDecl(expression: CodeExpression) {
        statementDecl(expression.toCodeBlock())
    }

    /**
     * Render a java statement.
     *
     * The provided code will have a semicolon and line break appended.
     *
     * @see [s]
     */
    fun statementDecl(block: CodeBlock) {
        endCurrentFlow()
        code.addStatement(block)
    }

    /**
     * Render a java statement.
     *
     * The provided code will have a semicolon and line break appended.
     *
     * @see [s]
     */
    fun statementDecl(block: CodeFunc) {
        statementDecl(codeBlock(block))
    }

    /**
     * Short alias for [statementDecl].
     */
    fun s(format: String, vararg args: Any) = statementDecl(expression(format, *args))

    /**
     * Short alias for [statementDecl].
     */
    fun s(expression: CodeExpression) = statementDecl(expression)

    /**
     * Short alias for [statementDecl].
     */
    fun s(block: CodeBlock) = statementDecl(block)

    /**
     * Short alias for [statementDecl].
     */
    fun s(block: CodeFunc) = statementDecl(block)

    /**
     * Declare a variable.
     *
     * @param type type of the variable
     * @param name name of the variable
     * @param initializer expression to initialize the variable
     * @param final true to declare the variable final
     *
     * @see [v]
     */
    fun variableDecl(type: TypeName, name: String, initializer: CodeExpression? = null, final: Boolean = false) {
        val prefix = if (final) "final " else ""

        if (initializer == null) {
            statementDecl("${prefix}%T %L", type, name)
        } else {
            statementDecl("${prefix}%T %L = %L", type, name, initializer)
        }
    }

    /**
     * Declare a variable.
     *
     * @param type type of the variable
     * @param name name of the variable
     * @param initializer expression to initialize the variable
     * @param final true to declare the variable final
     *
     * @see [v]
     */
    fun variableDecl(type: Type, name: String, initializer: CodeExpression? = null, final: Boolean = false) = variableDecl(TypeName.get(type), name, initializer, final)

    /**
     * Declare a variable.
     *
     * @param type type of the variable
     * @param name name of the variable
     * @param initializer expression to initialize the variable
     * @param final true to declare the variable final
     *
     * @see [v]
     */
    fun variableDecl(type: KClass<*>, name: String, initializer: CodeExpression? = null, final: Boolean = false) = variableDecl(TypeNames.of(type), name, initializer, final)

    /**
     * Short alias for [variableDecl].
     */
    fun v(type: TypeName, name: String, initializer: CodeExpression? = null, final: Boolean = false) = variableDecl(type, name, initializer, final)

    /**
     * Short alias for [variableDecl].
     */
    fun v(type: Type, name: String, initializer: CodeExpression? = null, final: Boolean = false) = variableDecl(TypeName.get(type), name, initializer, final)

    /**
     * Short alias for [variableDecl].
     */
    fun v(type: KClass<*>, name: String, initializer: CodeExpression? = null, final: Boolean = false) = variableDecl(TypeNames.of(type), name, initializer, final)

    private fun <T : Flow> beginFlow(flow: T, controlFlow: String, args: Array<out Any>, block: CodeFunc): T {
        endCurrentFlow()

        currentFlow = flow

        code.beginControlFlow(controlFlow, *args)
        buildBlock(code, block)

        return flow
    }

    /**
     * Render an if-else condition.
     *
     * The "else" and "else if" are declared by chaining calls to the returned [IfFlow].
     *
     * Example:
     *      ifDecl("var == %L", variableName) {
     *          s("return A")
     *      }.elseIfDecl("var == null") {
     *          s("return B")
     *      }.elseDecl {
     *          s("return C")
     *      }
     *
     *     // -- Result --
     *     // if (var == otherVar) {
     *     //   return A;
     *     // } else if (var == null) {
     *     //   return B;
     *     // } else {
     *     //   return C;
     *     // }
     */
    fun ifDecl(conditionFormat: String, vararg conditionArgs: Any, block: CodeFunc) = ifDecl(expression(conditionFormat, *conditionArgs), block)

    fun ifDecl(condition: CodeExpression, block: CodeFunc): IfFlow = beginFlow(IfFlow(), "if (\$L)", arrayOf(condition.toCodeBlock()), block)

    /**
     * Render a try-catch block.
     *
     * A "catch" and "finally" can be declared by chaining calls to the returned [TryFlow].
     *
     * Example:
     *      tryDecl {
     *          s("foo()")
     *      }.catchDecl(IllegalArgumentException::class, "ex") {
     *          s("bar()")
     *      }.catchDecl(types(NullPointerException::class, IllegalStateException::class), "ex") {
     *          s("bar()")
     *      }.finallyDecl {
     *          s("cleanup()")
     *      }
     *
     *     // -- Result --
     *     // try {
     *     //   foo();
     *     // } catch (final IllegalArgumentException ex) {
     *     //   bar();
     *     // } catch (final NullPointerException | IllegalStateException ex) {
     *     //   bar();
     *     // } finally {
     *     //   cleanup();
     *     // }
     */
    fun tryDecl(block: CodeFunc): TryFlow = beginFlow(TryFlow(), "try", emptyArray(), block)

    /**
     * Render an anonymous block.
     *
     * Example:
     *     blockDecl {
     *         s("int v = 7")
     *     }
     *
     *     // -- Result --
     *     // {
     *     //   int v = 7;
     *     // }
     */
    fun blockDecl(block: CodeFunc) {
        endCurrentFlow()

        code.add("{\n").indent()
        buildBlock(code, block)
        code.unindent().add("}\n")
    }

    fun forEachDecl(variableType: TypeName, variableName: String, collection: CodeExpression, block: CodeFunc) {
        endCurrentFlow()

        code.beginControlFlow("for (final \$T \$L : \$L)", variableType, variableName, collection.toCodeBlock())
        buildBlock(code, block)
        code.endControlFlow()
    }

    fun forEachDecl(variableType: Type, variableName: String, collection: CodeExpression, block: CodeFunc) = forEachDecl(TypeName.get(variableType), variableName, collection, block)

    fun forEachDecl(variableType: KClass<*>, variableName: String, collection: CodeExpression, block: CodeFunc) = forEachDecl(TypeNames.of(variableType), variableName, collection, block)

    fun whileDecl(condition: CodeExpression, block: CodeFunc) {
        endCurrentFlow()

        code.beginControlFlow("while (\$L)", condition.toCodeBlock())
        buildBlock(code, block)
        code.endControlFlow()
    }

    fun whileDecl(format: String, vararg args: Any, block: CodeFunc) = whileDecl(expression(format, *args), block)

    open inner class Flow {
        protected fun checkIsCurrentFlow() {
            check(currentFlow == this)
        }
    }

    inner class IfFlow : Flow() {
        /**
         * Render an "else if" block.
         */
        fun elseIfDecl(conditionFormat: String, vararg conditionArgs: Any, block: CodeFunc) = elseIfDecl(expression(conditionFormat, *conditionArgs), block)

        /**
         * Render an "else if" block.
         */
        fun elseIfDecl(condition: CodeExpression, block: CodeFunc): IfFlow {
            checkIsCurrentFlow()

            code.nextControlFlow("else if (\$L)", condition.toCodeBlock())
            buildBlock(code, block)

            return this
        }

        /**
         * Render a final "else" block.
         *
         * This ends the if flow.
         */
        fun elseDecl(block: CodeFunc) {
            checkIsCurrentFlow()

            code.nextControlFlow("else")
            buildBlock(code, block)

            endCurrentFlow()
        }
    }

    inner class TryFlow : Flow() {
        /**
         * Render a catch block.
         *
         * The exception variable is declared final.
         *
         * @param types Exception types to catch. More than one type will render a multi-catch.
         * @param variableName Exception variable name.
         */
        fun catchDecl(types: List<TypeName>, variableName: String, block: CodeFunc): TryFlow {
            checkIsCurrentFlow()

            val typesFormat = types.joinToString(" | ") { "\$T" }

            code.nextControlFlow("catch (final $typesFormat \$L)", *types.toTypedArray(), variableName)
            buildBlock(code, block)

            return this
        }

        /**
         * Render a catch block.
         *
         * The exception variable is declared final.
         *
         * @param type Exception type to catch.
         * @param variableName Exception variable name.
         */
        fun catchDecl(type: TypeName, variableName: String, block: CodeFunc) = catchDecl(listOf(type), variableName, block)

        /**
         * Render a catch block.
         *
         * The exception variable is declared final.
         *
         * @param type Exception type to catch.
         * @param variableName Exception variable name.
         */
        fun catchDecl(type: KClass<out Throwable>, variableName: String, block: CodeFunc) = catchDecl(TypeNames.types(type), variableName, block)

        /**
         * Render a "finally" block.
         *
         * This ends the try flow.
         */
        fun finallyDecl(block: CodeFunc) {
            checkIsCurrentFlow()

            code.nextControlFlow("finally")
            buildBlock(code, block)

            endCurrentFlow()
        }
    }
}