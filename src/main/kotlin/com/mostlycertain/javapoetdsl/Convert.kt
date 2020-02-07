package com.mostlycertain.javapoetdsl

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeName
import kotlin.reflect.KClass


/**
 * Convert a Kotlin [KClass] into a javapoet [TypeName].
 */
fun typeName(class_: KClass<*>): TypeName = when (class_) {
    Float::class -> TypeName.FLOAT
    Double::class -> TypeName.DOUBLE
    Boolean::class -> TypeName.BOOLEAN
    Char::class -> TypeName.CHAR
    Byte::class -> TypeName.BYTE
    Short::class -> TypeName.SHORT
    Int::class -> TypeName.INT
    Long::class -> TypeName.LONG
    Any::class -> TypeName.OBJECT
    else -> ClassName.get(class_.java)
}

fun typeList(vararg classes: KClass<*>): List<TypeName> = classes.map(::typeName)