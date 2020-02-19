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

    val ITERABLE = className("java.lang", "Iterable")

    val ITERATOR = className("java.util", "Iterator")

    val STREAM = className("java.util.stream", "Stream")

    /**
     * Annotation used to mark source code as generated.
     *
     * Available in JDK7 and removed in JDK9.
     *
     * @see GENERATED_JDK9
     * @see Annotations.generated
     */
    val GENERATED_JDK8 = className("javax.annotation", "Generated")

    /**
     * Annotation used to mark source code as generated.
     *
     * Introduced in JDK9 as a replacement for [GENERATED_JDK8].
     *
     * @see Annotations.generated
     */
    val GENERATED_JDK9 = className("javax.annotation.processing", "Generated")

    val FUNCTIONAL_INTERFACE = className("java.lang", "FunctionalInterface")

    val SAFE_VARARGS = className("java.lang", "SafeVarargs")

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

    fun arrayType(componentType: TypeName): ArrayTypeName {
        return ArrayTypeName.of(componentType)
    }

    fun optionalType(typeArgument: KClass<*>): ParameterizedTypeName {
        return parameterizedType(OPTIONAL, typeArgument)
    }

    fun optionalType(typeArgument: TypeName): ParameterizedTypeName {
        return parameterizedType(OPTIONAL, typeArgument)
    }

    fun listType(typeArgument: KClass<*>): ParameterizedTypeName {
        return parameterizedType(LIST, typeArgument)
    }

    fun listType(typeArgument: TypeName): ParameterizedTypeName {
        return parameterizedType(LIST, typeArgument)
    }

    fun setType(typeArgument: KClass<*>): ParameterizedTypeName {
        return parameterizedType(SET, typeArgument)
    }

    fun setType(typeArgument: TypeName): ParameterizedTypeName {
        return parameterizedType(SET, typeArgument)
    }

    fun iterableType(typeArgument: KClass<*>): ParameterizedTypeName {
        return parameterizedType(ITERABLE, typeArgument)
    }

    fun iterableType(typeArgument: TypeName): ParameterizedTypeName {
        return parameterizedType(ITERABLE, typeArgument)
    }

    fun iteratorType(typeArgument: KClass<*>): ParameterizedTypeName {
        return parameterizedType(ITERATOR, typeArgument)
    }

    fun iteratorType(typeArgument: TypeName): ParameterizedTypeName {
        return parameterizedType(ITERATOR, typeArgument)
    }

    fun streamType(typeArgument: KClass<*>): ParameterizedTypeName {
        return parameterizedType(STREAM, typeArgument)
    }

    fun streamType(typeArgument: TypeName): ParameterizedTypeName {
        return parameterizedType(STREAM, typeArgument)
    }

    fun mapType(keyType: KClass<*>, valueType: KClass<*>): ParameterizedTypeName {
        return parameterizedType(MAP, keyType, valueType)
    }

    fun mapType(keyType: TypeName, valueType: TypeName): ParameterizedTypeName {
        return parameterizedType(MAP, keyType, valueType)
    }

    fun parameterizedType(rawType: ClassName, vararg typeArguments: KClass<*>): ParameterizedTypeName {
        return ParameterizedTypeName.get(rawType, *typeArguments.map { of(it).ensureBoxed() }.toTypedArray())
    }

    fun parameterizedType(rawType: ClassName, vararg typeArguments: TypeName): ParameterizedTypeName {
        return ParameterizedTypeName.get(rawType, *typeArguments.map { it.ensureBoxed() }.toTypedArray())
    }
}