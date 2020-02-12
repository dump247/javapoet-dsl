package com.mostlycertain.javapoetdsl

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeName
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TypeNamesTest {
    @Test
    fun `of maps to predefined type names`() {
        Assertions.assertEquals(TypeName.BOOLEAN, TypeNames.of(Boolean::class))
        Assertions.assertEquals(TypeName.BYTE, TypeNames.of(Byte::class))
        Assertions.assertEquals(TypeName.SHORT, TypeNames.of(Short::class))
        Assertions.assertEquals(TypeName.INT, TypeNames.of(Int::class))
        Assertions.assertEquals(TypeName.LONG, TypeNames.of(Long::class))
        Assertions.assertEquals(TypeName.FLOAT, TypeNames.of(Float::class))
        Assertions.assertEquals(TypeName.DOUBLE, TypeNames.of(Double::class))
        Assertions.assertEquals(TypeName.CHAR, TypeNames.of(Char::class))
        Assertions.assertEquals(TypeName.OBJECT, TypeNames.of(Any::class))
        Assertions.assertEquals(TypeName.OBJECT, TypeNames.of(Object::class))
        Assertions.assertEquals(TypeName.VOID, TypeNames.of(Void::class))
    }

    @Test
    fun `of maps types`() {
        Assertions.assertEquals(ClassName.get(java.lang.String::class.java), TypeNames.of(String::class))
        Assertions.assertEquals(TypeName.INT, TypeNames.of(Integer::class))
        Assertions.assertEquals(TypeName.CHAR, TypeNames.of(Character::class))
    }

    @Test
    fun `listOf maps types`() {
        Assertions.assertEquals(emptyList<TypeName>(), TypeNames.types())
        Assertions.assertEquals(listOf(TypeName.INT), TypeNames.types(Int::class))
        Assertions.assertEquals(listOf(TypeName.INT, TypeName.LONG, ClassName.get(java.lang.String::class.java)), TypeNames.types(Int::class, Long::class, String::class))
    }

    @Test
    fun `ensureBoxed maps boxed types`() {
        Assertions.assertEquals(ClassName.get(java.lang.String::class.java), ClassName.get(String::class.java).ensureBoxed())
        Assertions.assertEquals(ClassName.get(Integer::class.java), TypeName.INT.ensureBoxed())
        Assertions.assertEquals(ClassName.get(Character::class.java), TypeName.CHAR.ensureBoxed())
    }

    @Test
    fun `ensureUnboxed maps boxed types`() {
        Assertions.assertEquals(ClassName.get(java.lang.String::class.java), ClassName.get(String::class.java).ensureUnboxed())
        Assertions.assertEquals(TypeName.INT, ClassName.get(Integer::class.java).ensureUnboxed())
        Assertions.assertEquals(TypeName.CHAR, ClassName.get(Character::class.java).ensureUnboxed())
    }
}