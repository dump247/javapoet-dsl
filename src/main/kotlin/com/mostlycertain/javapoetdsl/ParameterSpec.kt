package com.mostlycertain.javapoetdsl

import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeName
import java.lang.reflect.Type
import javax.lang.model.element.Modifier.FINAL
import kotlin.reflect.KClass

private val MODIFIERS_FINAL = listOf(FINAL)

fun parameterSpec(
        type: TypeName,
        name: String,
        final: Boolean = true,
        annotations: List<AnnotationSpec> = emptyList()
): ParameterSpec = ParameterSpec
        .builder(type, name)
        .addModifiers(if (final) MODIFIERS_FINAL else emptyList())
        .addAnnotations(annotations)
        .build()

fun parameterSpec(
        type: Type,
        name: String,
        final: Boolean = true,
        annotations: List<AnnotationSpec> = emptyList()
) = parameterSpec(TypeName.get(type), name, final, annotations)

fun parameterSpec(
        type: KClass<*>,
        name: String,
        final: Boolean = true,
        annotations: List<AnnotationSpec> = emptyList()
) = parameterSpec(typeName(type), name, final, annotations)
