package com.mostlycertain.javapoetdsl

import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeName
import java.nio.file.Path
import java.nio.file.Paths

/**
 * If the type is primitive (e.g. int, char), return the boxed type (e.g. Integer, Character).
 * Otherwise, return `this`.
 *
 * This is an alias for [TypeName.box] created to match [ensureUnboxed].
 */
fun TypeName.ensureBoxed(): TypeName = box()

/**
 * If the type is a boxed primitive (e.g. Integer, Character), return the primitive type (e.g. int, char).
 * Otherwise, return `this`.
 */
fun TypeName.ensureUnboxed(): TypeName = if (isBoxedPrimitive) unbox() else this

/**
 * Type of the annotation.
 *
 * This is [AnnotationSpec.type] cast to [ClassName] because an annotation can not be anything but a class name.
 */
val AnnotationSpec.className: ClassName
    get() = type as ClassName

/**
 * True if the class name is a nested class.
 */
val ClassName.isNested: Boolean
    get() = enclosingClassName() != null

/**
 * Convert a non-nested class name to a java file path.
 *
 * Example:
 *   a.b.c.Foo => a/b/c/Foo.java
 *
 * @throws IllegalStateException if this class name is a nested class
 */
fun ClassName.toFilePath(): Path {
    if (isNested) {
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
