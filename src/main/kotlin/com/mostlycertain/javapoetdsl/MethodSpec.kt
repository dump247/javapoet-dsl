package com.mostlycertain.javapoetdsl

import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeName
import java.lang.reflect.Type
import javax.lang.model.element.Modifier
import kotlin.reflect.KClass

typealias MethodCodeFunc = MethodCodeBuilder.() -> Unit

private fun methodSpecInternal(
        returns: TypeName,
        name: String,
        parameters: List<ParameterSpec> = emptyList(),
        modifiers: List<Modifier> = emptyList(),
        annotations: List<AnnotationSpec> = emptyList(),
        throws: List<TypeName> = emptyList(),
        varargs: Boolean = false,
        body: MethodCodeFunc? = null
): MethodSpec {
    val methodBuilder = MethodSpec
            .methodBuilder(name)
            .returns(returns)
            .addParameters(parameters)
            .addModifiers(modifiers)
            .addAnnotations(annotations)
            .addExceptions(throws)
            .varargs(varargs)

    val code = CodeBlock.builder()
    val builder = MethodCodeBuilder(MethodMeta(name, returns, parameters, modifiers, annotations, varargs, throws), code)
    if (body != null) {
        builder.body()
    }
    builder.close()
    methodBuilder.addCode(code.build())

    return methodBuilder.build()
}

fun methodSpec(
        name: String,
        parameters: List<ParameterSpec> = emptyList(),
        modifiers: List<Modifier> = emptyList(),
        annotations: List<AnnotationSpec> = emptyList(),
        throws: List<TypeName> = emptyList(),
        varargs: Boolean = false,
        body: MethodCodeFunc? = null
) = methodSpecInternal(TypeName.VOID, name, parameters, modifiers, annotations, throws, varargs, body)

fun methodSpec(
        returns: TypeName,
        name: String,
        parameters: List<ParameterSpec> = emptyList(),
        modifiers: List<Modifier> = emptyList(),
        annotations: List<AnnotationSpec> = emptyList(),
        throws: List<TypeName> = emptyList(),
        varargs: Boolean = false,
        body: MethodCodeFunc? = null
) = methodSpecInternal(returns, name, parameters, modifiers, annotations, throws, varargs, body)

fun methodSpec(
        returns: Type,
        name: String,
        parameters: List<ParameterSpec> = emptyList(),
        modifiers: List<Modifier> = emptyList(),
        annotations: List<AnnotationSpec> = emptyList(),
        throws: List<TypeName> = emptyList(),
        varargs: Boolean = false,
        body: MethodCodeFunc? = null
) = methodSpecInternal(TypeName.get(returns), name, parameters, modifiers, annotations, throws, varargs, body)

fun methodSpec(
        returns: KClass<*>,
        name: String,
        parameters: List<ParameterSpec> = emptyList(),
        modifiers: List<Modifier> = emptyList(),
        annotations: List<AnnotationSpec> = emptyList(),
        throws: List<TypeName> = emptyList(),
        varargs: Boolean = false,
        body: MethodCodeFunc? = null
) = methodSpecInternal(TypeNames.of(returns), name, parameters, modifiers, annotations, throws, varargs, body)

data class MethodMeta(
        val name: String,
        val returns: TypeName,
        val parameters: List<ParameterSpec>,
        val modifiers: List<Modifier>,
        val annotations: List<AnnotationSpec>,
        val varargs: Boolean,
        val throws: List<TypeName>
)

class MethodCodeBuilder(val methodMeta: MethodMeta, code: CodeBlock.Builder) : CodeBuilder(code)