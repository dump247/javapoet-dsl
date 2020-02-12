package com.mostlycertain.javapoetdsl

import com.squareup.javapoet.ArrayTypeName
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import kotlin.reflect.KClass

object TypeNames {
    val PARAMETERS_ARE_NONNULL_BY_DEFAULT: ClassName = ClassName.get(
            "javax.annotation",
            "ParametersAreNonnullByDefault")

    val NULLABLE: ClassName = ClassName.get(
            "javax.annotation",
            "Nullable")

    val NONNULL: ClassName = ClassName.get(
            "javax.annotation",
            "Nonnull")

    val DEPRECATED: ClassName = ClassName.get(
            "java.lang",
            "Deprecated")

    val OVERRIDE: ClassName = ClassName.get(
            "java.lang",
            "Override")

    val SUPPRESS_WARNINGS: ClassName = ClassName.get(
            "java.lang",
            "SuppressWarnings")

    val IMMUTABLE: ClassName = ClassName.get(
            "javax.annotation.concurrent",
            "Immutable")

    val CHECK_RETURN_VALUE: ClassName = ClassName.get(
            "javax.annotation",
            "CheckReturnValue")

    val GENERATED: ClassName = ClassName.get(
            "javax.annotation",
            "Generated")

    val OPTIONAL: ClassName = ClassName.get(
            "java.util",
            "Optional")

    val LIST: ClassName = ClassName.get(
            "java.util",
            "List")

    val SET: ClassName = ClassName.get(
            "java.util",
            "Set")

    val MAP: ClassName = ClassName.get(
            "java.util",
            "Map")

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