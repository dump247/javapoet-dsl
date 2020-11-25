package com.mostlycertain.javapoetdsl

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CodeBuilderTest {
    @Test
    fun `example in readme`() {
        val variableName = "counter"

        val block = codeBlock {
            v(Int::class, variableName, literal(1))

            whileDecl("%L < 10", variableName) {
                ifDecl("%L %% 2 == 0", variableName) {
                    s("%T.out.printf(%S, %L)", System::class, "even: %d\n", variableName)
                }.elseDecl {
                    s("%T.out.printf(%S, %L)", System::class, "odd: %d\n", variableName)
                }

                s("%L += 1", variableName)
            }
        }

        assertEquals("""
            int counter = 1;
            while (counter < 10) {
              if (counter % 2 == 0) {
                java.lang.System.out.printf("even: %d\n", counter);
              } else {
                java.lang.System.out.printf("odd: %d\n", counter);
              }
              counter += 1;
            }

        """.trimIndent(), block.toString())
    }

    @Test
    fun `lambdaDecl with block and no parameters`() {
        val block = codeBlock {
            lambdaDecl {
                s("return 1")
            }
        }

        assertEquals("""
            () -> {
              return 1;
            }
        """.trimIndent(), block.toString())
    }

    @Test
    fun `lambdaDecl with block and one parameter`() {
        val block = codeBlock {
            lambdaDecl("param1") {
                s("return 1")
            }
        }

        assertEquals("""
            (param1) -> {
              return 1;
            }
        """.trimIndent(), block.toString())
    }

    @Test
    fun `lambdaDecl with block and multiple parameters`() {
        val block = codeBlock {
            lambdaDecl("param1", "param2", "param3", "param4", "param5") {
                s("return 1")
            }
        }

        assertEquals("""
            (param1, param2, param3, param4, param5) -> {
              return 1;
            }
        """.trimIndent(), block.toString())
    }

    @Test
    fun `lambdaDecl with expression and no parameters`() {
        val block = codeBlock {
            lambdaDecl(literal("hi"))
            write("\n")
            lambdaDecl(parameterNames = *emptyArray(), expression = literal(17))
        }

        assertEquals("""
            () -> "hi"
            () -> 17
        """.trimIndent(), block.toString())
    }

    @Test
    fun `lambdaDecl with expression and one parameter`() {
        val block = codeBlock {
            lambdaDecl("param1", expression = literal(17))
        }

        assertEquals("""
            (param1) -> 17
        """.trimIndent(), block.toString())
    }

    @Test
    fun `lambdaDecl with expression and multiple parameters`() {
        val block = codeBlock {
            lambdaDecl("param1", "param2", "param3", "param4", "param5", expression = literal(17))
        }

        assertEquals("""
            (param1, param2, param3, param4, param5) -> 17
        """.trimIndent(), block.toString())
    }
}