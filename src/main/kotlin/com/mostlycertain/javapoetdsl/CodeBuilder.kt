package com.mostlycertain.javapoetdsl

import com.squareup.javapoet.ClassName
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
        endCurrentFlow()
        code.addStatement(expression.toCodeBlock())
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
    fun variableDecl(type: KClass<*>, name: String, initializer: CodeExpression? = null, final: Boolean = false) = variableDecl(typeName(type), name, initializer, final)

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
    fun v(type: KClass<*>, name: String, initializer: CodeExpression? = null, final: Boolean = false) = variableDecl(typeName(type), name, initializer, final)

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
     *      }.catchDecl(listOf(NullPointerException::class, IllegalStateException::class), "ex") {
     *          s("bar()")
     *      }.finallyDecl {
     *          s("cleanup()")
     *      }
     */
    fun tryDecl(block: CodeFunc): TryFlow = beginFlow(TryFlow(), "try", emptyArray(), block)

    /**
     * Render an anonymous block.
     *
     * Example:
     *     blockDecl {
     *         s("int v")
     *     }
     */
    fun blockDecl(block: CodeFunc) {
        endCurrentFlow()

        code.add("{\n").indent();
        buildBlock(code, block)
        code.unindent().add("}\n");
    }

    fun forEachDecl(variableType: TypeName, variableName: String, collection: CodeExpression, block: CodeFunc) {
        endCurrentFlow()

        code.beginControlFlow("for (final \$T \$L : \$L)", variableType, variableName, collection.toCodeBlock())
        buildBlock(code, block)
        code.endControlFlow()
    }

    fun forEachDecl(variableType: Type, variableName: String, collection: CodeExpression, block: CodeFunc) = forEachDecl(TypeName.get(variableType), variableName, collection, block)

    fun forEachDecl(variableType: KClass<*>, variableName: String, collection: CodeExpression, block: CodeFunc) = forEachDecl(typeName(variableType), variableName, collection, block)

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
        fun catchDecl(type: KClass<out Throwable>, variableName: String, block: CodeFunc) = catchDecl(listOf(typeName(type)), variableName, block)

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