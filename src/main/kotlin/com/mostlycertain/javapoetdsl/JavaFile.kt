package com.mostlycertain.javapoetdsl

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import java.nio.file.Path
import java.nio.file.Paths

fun ClassName.toPath(): Path {
    if (enclosingClassName() != null) {
        throw IllegalStateException("Can not generate a path for an inner class: $this")
    }

    return Paths.get(packageName().replace('.', '/'), "${simpleName()}.java")
}

fun JavaFile.writeToString(): String {
    val result = StringBuilder()
    writeTo(result)
    return result.toString()
}
