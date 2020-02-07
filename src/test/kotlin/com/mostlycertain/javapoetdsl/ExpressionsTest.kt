package com.mostlycertain.javapoetdsl

import com.squareup.javapoet.CodeBlock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

class ExpressionsTest {
    @Test
    fun `emptyExpression is empty`() {
        assertTrue(emptyExpression().toCodeBlock().isEmpty)
    }

    @Test
    fun `expression and e resolve formatting`() {
        assertAll(
                { assertEquals(CodeBlock.of(""), expression("").toCodeBlock()) },
                { assertEquals(CodeBlock.of(""), e("").toCodeBlock()) },

                { assertEquals(CodeBlock.of("%"), expression("%%").toCodeBlock()) },
                { assertEquals(CodeBlock.of("%"), e("%%").toCodeBlock()) },

                { assertEquals(CodeBlock.of("\$L", "lit"), expression("%L", "lit").toCodeBlock()) },
                { assertEquals(CodeBlock.of("\$L", "lit"), e("%L", "lit").toCodeBlock()) },

                {
                    assertEquals(
                            CodeBlock.of("(\$L) - \$S", CodeBlock.of("\$L", 17), "string value"),
                            expression("(%L) - %S", expression("%L", 17), "string value").toCodeBlock())
                },
                {
                    assertEquals(
                            CodeBlock.of("(\$L) - \$S", CodeBlock.of("\$L", 17), "string value"),
                            expression("(%L) - %S", e("%L", 17), "string value").toCodeBlock())
                },
                {
                    assertEquals(
                            CodeBlock.of("(\$L) - \$S", CodeBlock.of("\$L", 17), "string value"),
                            e("(%L) - %S", expression("%L", 17), "string value").toCodeBlock())
                },
                {
                    assertEquals(
                            CodeBlock.of("(\$L) - \$S", CodeBlock.of("\$L", 17), "string value"),
                            e("(%L) - %S", e("%L", 17), "string value").toCodeBlock())
                }
        )
    }

    @Test
    fun `literal String`() {
        assertAll(
                { assertEquals(CodeBlock.of("\$S", ""), literal("").toCodeBlock()) },
                { assertEquals(CodeBlock.of("\$S", "A"), literal("A").toCodeBlock()) },
                { assertEquals(CodeBlock.of("\$S", "\uFEFF"), literal("\uFEFF").toCodeBlock()) },
                { assertEquals(CodeBlock.of("\$S", "\nSome string value\r"), literal("\nSome string value\r").toCodeBlock()) }
        )
    }

    @Test
    fun `literal Char`() {
        assertAll(
                { assertEquals(CodeBlock.of("'\$L'", "A"), literal('A').toCodeBlock()) },
                { assertEquals(CodeBlock.of("'\$L'", "\uFEFF"), literal('\uFEFF').toCodeBlock()) },
                { assertEquals(CodeBlock.of("'\\n'"), literal('\n').toCodeBlock()) },
                { assertEquals(CodeBlock.of("'\\r'"), literal('\r').toCodeBlock()) },
                { assertEquals(CodeBlock.of("'\\''"), literal('\'').toCodeBlock()) }
        )
    }

    @Test
    fun `literal Double`() {
        assertAll(
                { assertEquals(CodeBlock.of("\$LD", Double.MIN_VALUE), literal(Double.MIN_VALUE).toCodeBlock()) },
                { assertEquals(CodeBlock.of("\$LD", -0.1), literal(-0.1).toCodeBlock()) },
                { assertEquals(CodeBlock.of("\$LD", 0.0), literal(0.0).toCodeBlock()) },
                { assertEquals(CodeBlock.of("\$LD", 0.1), literal(0.1).toCodeBlock()) },
                { assertEquals(CodeBlock.of("\$LD", Double.MAX_VALUE), literal(Double.MAX_VALUE).toCodeBlock()) },
                { assertEquals(CodeBlock.of("\$T.NaN", java.lang.Double::class.java), literal(Double.NaN).toCodeBlock()) },
                { assertEquals(CodeBlock.of("\$T.POSITIVE_INFINITY", java.lang.Double::class.java), literal(Double.POSITIVE_INFINITY).toCodeBlock()) },
                { assertEquals(CodeBlock.of("\$T.NEGATIVE_INFINITY", java.lang.Double::class.java), literal(Double.NEGATIVE_INFINITY).toCodeBlock()) }
        )
    }

    @Test
    fun `literal Float`() {
        assertAll(
                { assertEquals(CodeBlock.of("\$L", Float.MIN_VALUE), literal(Float.MIN_VALUE).toCodeBlock()) },
                { assertEquals(CodeBlock.of("\$L", -0.1F), literal(-0.1F).toCodeBlock()) },
                { assertEquals(CodeBlock.of("\$L", 0.0F), literal(0.0F).toCodeBlock()) },
                { assertEquals(CodeBlock.of("\$L", 0.1F), literal(0.1F).toCodeBlock()) },
                { assertEquals(CodeBlock.of("\$L", Float.MAX_VALUE), literal(Float.MAX_VALUE).toCodeBlock()) },
                { assertEquals(CodeBlock.of("\$T.NaN", java.lang.Float::class.java), literal(Float.NaN).toCodeBlock()) },
                { assertEquals(CodeBlock.of("\$T.POSITIVE_INFINITY", java.lang.Float::class.java), literal(Float.POSITIVE_INFINITY).toCodeBlock()) },
                { assertEquals(CodeBlock.of("\$T.NEGATIVE_INFINITY", java.lang.Float::class.java), literal(Float.NEGATIVE_INFINITY).toCodeBlock()) }
        )
    }

    @Test
    fun `literal Boolean`() {
        assertAll(
                { assertEquals(CodeBlock.of("true"), literal(true).toCodeBlock()) },
                { assertEquals(CodeBlock.of("false"), literal(false).toCodeBlock()) }
        )
    }
}