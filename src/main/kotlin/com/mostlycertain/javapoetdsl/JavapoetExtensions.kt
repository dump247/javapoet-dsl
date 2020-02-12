package com.mostlycertain.javapoetdsl

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeName
import java.nio.file.Path
import java.nio.file.Paths

/**
 * If the type is primitive (e.g. int, char), return the boxed type (e.g. Integer, Character).
 * Otherwise, return `this`.
 */
fun TypeName.ensureBoxed(): TypeName = if (isPrimitive) box() else this

/**
 * If the type is a boxed primitive (e.g. Integer, Character), return the primitive type (e.g. int, char).
 * Otherwise, return `this`.
 */
fun TypeName.ensureUnboxed(): TypeName = if (isBoxedPrimitive) unbox() else this

/**
 * Convert a non-nested class name to a java file path.
 *
 * Example:
 *   a.b.c.Foo => a/b/c/Foo.java
 *
 * @throws IllegalStateException if this class name is a nested class
 */
fun ClassName.toFilePath(): Path {
    if (enclosingClassName() != null) {
        throw IllegalStateException("Can not generate a path for an inner class: $this")
    }

    return Paths.get(packageName().replace('.', '/'), "${simpleName()}.java")
}

/**
 * Write the generated file content to a string.
 */
fun JavaFile.writeToString(): String {
    val result = StringBuilder()
    writeTo(result)
    return result.toString()
}
