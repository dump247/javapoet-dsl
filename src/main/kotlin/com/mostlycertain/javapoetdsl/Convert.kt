package com.mostlycertain.javapoetdsl

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeName
import kotlin.reflect.KClass


/**
 * Convert a Kotlin [KClass] into a javapoet [TypeName].
 */
fun typeName(class_: KClass<*>, boxed: Boolean = false): TypeName {
    val typeName = when (class_) {
        Float::class -> TypeName.FLOAT
        Double::class -> TypeName.DOUBLE
        Boolean::class -> TypeName.BOOLEAN
        Char::class -> TypeName.CHAR
        Byte::class -> TypeName.BYTE
        Short::class -> TypeName.SHORT
        Int::class -> TypeName.INT
        Long::class -> TypeName.LONG
        Any::class -> TypeName.OBJECT
        java.lang.Object::class -> TypeName.OBJECT
        Void::class -> TypeName.VOID
        else -> ClassName.get(class_.java)
    }

    return if (boxed && typeName.isPrimitive) typeName.box() else typeName
}

fun typeList(vararg classes: KClass<*>): List<TypeName> = classes.map { typeName(it) }