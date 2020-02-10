package com.mostlycertain.javapoetdsl

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import java.lang.IllegalArgumentException
import java.nio.file.Path
import java.nio.file.Paths

fun javaFilePath(className: ClassName): Path {
    if (className.enclosingClassName() != null) {
        throw IllegalArgumentException("Can not generate a path for an inner class: $className")
    }

    return Paths.get(
            className.packageName().replace('.', '/'),
            "${className.simpleName()}.java")
}

fun JavaFile.writeToString(): String {
    val result = StringBuilder()
    writeTo(result)
    return result.toString()
}
