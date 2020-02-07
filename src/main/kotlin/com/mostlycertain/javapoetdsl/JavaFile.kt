package com.mostlycertain.javapoetdsl

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import java.nio.file.Path
import java.nio.file.Paths

fun javaFilePath(classSpec: ClassSpec) = javaFilePath(classSpec.className)

fun javaFilePath(className: ClassName): Path {
    var filePath = Paths.get("")

    for (packageComponent in className.packageName().split("\\.".toRegex())) {
        filePath = filePath.resolve(packageComponent)
    }

    return filePath.resolve("${className.simpleName()}.java")
}

fun javaFileBuilder(classSpec: ClassSpec): JavaFile.Builder = JavaFile.builder(classSpec.className.packageName(), classSpec.typeSpec)

fun JavaFile.writeToString(): String {
    val result = StringBuilder()
    writeTo(result)
    return result.toString()
}
