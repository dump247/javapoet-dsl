package com.mostlycertain.javapoetdsl

import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.TypeName
import java.lang.reflect.Type
import javax.lang.model.element.Modifier
import kotlin.reflect.KClass

fun fieldSpec(
        type: TypeName,
        name: String,
        modifiers: List<Modifier> = emptyList(),
        annotations: List<AnnotationSpec> = emptyList(),
        initializer: CodeExpression? = null
): FieldSpec {
    val fieldBuilder = FieldSpec.builder(type, name)
            .addModifiers(*modifiers.toTypedArray()) // no Iterable overload available
            .addAnnotations(annotations)

    if (initializer != null) {
        // Currently, javapoet indents field initializers to the same level as the field declaration.
        // Indent so that if the initializer contains a line break, the lines are indented one level.
        fieldBuilder.initializer(CodeBlock.of("\$>\$L\$<", initializer.toCodeBlock()))
    }

    return fieldBuilder.build()
}

fun fieldSpec(
        type: Type,
        name: String,
        modifiers: List<Modifier> = emptyList(),
        annotations: List<AnnotationSpec> = emptyList(),
        initializer: CodeExpression? = null
) = fieldSpec(TypeName.get(type), name, modifiers, annotations, initializer)

fun fieldSpec(
        type: KClass<*>,
        name: String,
        modifiers: List<Modifier> = emptyList(),
        annotations: List<AnnotationSpec> = emptyList(),
        initializer: CodeExpression? = null
) = fieldSpec(TypeNames.of(type), name, modifiers, annotations, initializer)
