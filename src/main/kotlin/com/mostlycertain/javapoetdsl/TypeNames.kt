package com.mostlycertain.javapoetdsl

import com.squareup.javapoet.ArrayTypeName
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import kotlin.reflect.KClass

object TypeNames {
    val DEPRECATED = className("java.lang", "Deprecated")

    val OVERRIDE = className("java.lang", "Override")

    val SUPPRESS_WARNINGS = className("java.lang", "SuppressWarnings")

    val OPTIONAL = className("java.util", "Optional")

    val LIST = className("java.util", "List")

    val SET = className("java.util", "Set")

    val MAP = className("java.util", "Map")

    val GENERATED = className("javax.annotation", "Generated")

    fun className(packageName: String, simpleName: String, vararg simpleNames: String): ClassName = ClassName.get(packageName, simpleName, *simpleNames)

    fun of(class_: KClass<*>): TypeName {
        // TODO return constant types declared in [TypeNames]?
        // TODO cache result? ClassName.get does not cache
        return when (class_) {
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
    }

    fun types(vararg classes: KClass<*>): List<TypeName> = classes.map(TypeNames::of)

    fun arrayType(componentType: KClass<*>): ArrayTypeName {
        return ArrayTypeName.of(of(componentType))
    }

    fun optionalType(typeArgument: KClass<*>): ParameterizedTypeName {
        return optionalType(of(typeArgument))
    }

    fun optionalType(typeArgument: TypeName): ParameterizedTypeName {
        return ParameterizedTypeName.get(OPTIONAL, typeArgument.ensureBoxed())
    }

    fun listType(typeArgument: KClass<*>): ParameterizedTypeName {
        return listType(of(typeArgument))
    }

    fun listType(typeArgument: TypeName): ParameterizedTypeName {
        return ParameterizedTypeName.get(LIST, typeArgument.ensureBoxed())
    }

    fun setType(typeArgument: KClass<*>): ParameterizedTypeName {
        return setType(of(typeArgument))
    }

    fun setType(typeArgument: TypeName): ParameterizedTypeName {
        return ParameterizedTypeName.get(SET, typeArgument.ensureBoxed())
    }

    fun mapType(keyType: KClass<*>, valueType: KClass<*>): ParameterizedTypeName {
        return mapType(of(keyType), of(valueType))
    }

    fun mapType(keyType: TypeName, valueType: TypeName): ParameterizedTypeName {
        return ParameterizedTypeName.get(MAP, keyType.ensureBoxed(), valueType.ensureBoxed())
    }
}