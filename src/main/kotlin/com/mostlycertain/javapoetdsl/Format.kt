package com.mostlycertain.javapoetdsl

import com.squareup.javapoet.CodeBlock
import kotlin.reflect.KClass

object Format {
    interface JavaCodeBlock {
        fun toCodeBlock(): CodeBlock
    }

    /**
     * Processes arguments to a javapoet [CodeBlock] to transform kotlin and other types to types usable by javapoet.
     */
    internal fun transformArgs(args: Array<out Any>): Array<out Any> {
        return args.map {
            when (it) {
                is KClass<*> -> it.java
                is JavaCodeBlock -> it.toCodeBlock()
                else -> it
            }
        }.toTypedArray()
    }

    /**
     * Convert percent signs (%), used as the formatting marker in this library, to dollar sign ($), which is used as the
     * formatting marker in javapoet.
     */
    internal fun normalizeFormat(format: String): String {
        return format.replace(Regex("""\$|%.|%$""")) {
            when (it.value) {
                "$" -> "$$"
                "%%" -> "%"
                "%" -> "$"
                else -> "$" + it.value[1]
            }
        }
    }
}