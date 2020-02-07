package com.mostlycertain.javapoetdsl

import com.squareup.javapoet.CodeBlock
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

class FormatTest {
    @Test
    fun `transformArgs converts types to javapoet compatible values`() {
        val block = CodeBlock.of("a")

        assertAll(listOf(
                Pair(arrayOf<Any>(), arrayOf<Any>()),
                Pair(arrayOf(FormatTest::class.java), arrayOf(FormatTest::class)),
                Pair(arrayOf(block), arrayOf(TestCodeBlock(block))),
                Pair(arrayOf(block), arrayOf(block)),
                Pair(arrayOf("%L"), arrayOf("%L")),
                Pair(arrayOf("\$L"), arrayOf("\$L")),
                Pair(arrayOf(17), arrayOf(17)),
                Pair(arrayOf(FormatTest::class.java, block, block, "str", 17), arrayOf(FormatTest::class, TestCodeBlock(block), block, "str", 17))
        ).map {
            { assertArrayEquals(it.first, Format.transformArgs(it.second)) }
        })
    }

    @Test
    fun `normalizeFormat converts percent signs to dollar signs`() {
        assertAll(listOf(
                Pair("", ""),
                Pair("\$", "%"),
                Pair("%", "%%"),
                Pair("\$L", "%L"),
                Pair("\$\$", "\$"),
                Pair("$ \$L \$N \$T % %L \$\$\$\$ \$\$ $", "% %L %N %T %% %%L $$ $ %")
        ).map {
            { assertEquals(it.first, Format.normalizeFormat(it.second)) }
        })
    }

    class TestCodeBlock(val block: CodeBlock) : Format.JavaCodeBlock {
        override fun toCodeBlock() = block
    }
}
