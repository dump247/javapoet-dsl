package com.mostlycertain.javapoetdsl

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeName
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ConvertTest {
    @Test
    fun `typeName maps to predefined type names`() {
        assertEquals(TypeName.BOOLEAN, typeName(Boolean::class))
        assertEquals(TypeName.BYTE, typeName(Byte::class))
        assertEquals(TypeName.SHORT, typeName(Short::class))
        assertEquals(TypeName.INT, typeName(Int::class))
        assertEquals(TypeName.LONG, typeName(Long::class))
        assertEquals(TypeName.FLOAT, typeName(Float::class))
        assertEquals(TypeName.DOUBLE, typeName(Double::class))
        assertEquals(TypeName.CHAR, typeName(Char::class))
        assertEquals(TypeName.OBJECT, typeName(Any::class))
        assertEquals(TypeName.OBJECT, typeName(java.lang.Object::class))
        assertEquals(TypeName.VOID, typeName(Void::class))
    }

    @Test
    fun `typeName maps types`() {
        assertEquals(ClassName.get(java.lang.String::class.java), typeName(String::class))
    }

    @Test
    fun `typeList maps types`() {
        assertEquals(emptyList<TypeName>(), typeList())
        assertEquals(listOf(TypeName.INT), typeList(Int::class))
        assertEquals(listOf(TypeName.INT, TypeName.LONG, ClassName.get(java.lang.String::class.java)), typeList(Int::class, Long::class, String::class))
    }
}