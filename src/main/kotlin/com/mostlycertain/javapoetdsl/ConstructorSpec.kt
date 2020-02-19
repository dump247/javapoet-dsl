package com.mostlycertain.javapoetdsl

import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeName
import javax.lang.model.element.Modifier

typealias ConstructorCodeFunc = ConstructorCodeBuilder.() -> Unit

fun constructorSpec(
        parameters: List<ParameterSpec> = emptyList(),
        modifiers: List<Modifier> = emptyList(),
        annotations: List<AnnotationSpec> = emptyList(),
        throws: List<TypeName> = emptyList(),
        varargs: Boolean = false,
        body: ConstructorCodeFunc
): MethodSpec {
    val methodBuilder = MethodSpec
            .constructorBuilder()
            .addParameters(parameters)
            .addModifiers(modifiers)
            .addAnnotations(annotations)
            .addExceptions(throws)
            .varargs(varargs)

    val code = CodeBlock.builder()
    val builder = ConstructorCodeBuilder(ConstructorMeta(parameters, modifiers, annotations, throws, varargs), code)
    builder.body()
    builder.close()
    methodBuilder.addCode(code.build())

    return methodBuilder.build()
}

data class ConstructorMeta(
        val parameters: List<ParameterSpec>,
        val modifiers: List<Modifier>,
        val annotations: List<AnnotationSpec>,
        val throws: List<TypeName>,
        val varargs: Boolean
)

class ConstructorCodeBuilder(val constructorMeta: ConstructorMeta, code: CodeBlock.Builder) : CodeBuilder(code)