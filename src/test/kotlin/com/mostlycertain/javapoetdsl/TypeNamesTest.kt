package com.mostlycertain.javapoetdsl

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeName
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TypeNamesTest {
    @Test
    fun `of maps to predefined type names`() {
        assertEquals(TypeName.BOOLEAN, TypeNames.of(Boolean::class))
        assertEquals(TypeName.BYTE, TypeNames.of(Byte::class))
        assertEquals(TypeName.SHORT, TypeNames.of(Short::class))
        assertEquals(TypeName.INT, TypeNames.of(Int::class))
        assertEquals(TypeName.LONG, TypeNames.of(Long::class))
        assertEquals(TypeName.FLOAT, TypeNames.of(Float::class))
        assertEquals(TypeName.DOUBLE, TypeNames.of(Double::class))
        assertEquals(TypeName.CHAR, TypeNames.of(Char::class))
        assertEquals(TypeName.OBJECT, TypeNames.of(Any::class))
        assertEquals(TypeName.OBJECT, TypeNames.of(Object::class))
        assertEquals(TypeName.VOID, TypeNames.of(Void::class))
    }

    @Test
    fun `of maps types`() {
        assertEquals(ClassName.get(java.lang.String::class.java), TypeNames.of(String::class))
        assertEquals(TypeName.INT, TypeNames.of(Integer::class))
        assertEquals(TypeName.CHAR, TypeNames.of(Character::class))
    }

    @Test
    fun `listOf maps types`() {
        assertEquals(emptyList<TypeName>(), TypeNames.types())
        assertEquals(listOf(TypeName.INT), TypeNames.types(Int::class))
        assertEquals(listOf(TypeName.INT, TypeName.LONG, ClassName.get(java.lang.String::class.java)), TypeNames.types(Int::class, Long::class, String::class))
    }
}