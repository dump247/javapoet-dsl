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
        returns: TypeName? = null,
        name: String,
        parameters: List<ParameterSpec> = emptyList(),
        modifiers: List<Modifier> = emptyList(),
        annotations: List<AnnotationSpec> = emptyList(),
        exceptions: List<TypeName> = emptyList(),
        varargs: Boolean = false,
        block: MethodCodeFunc
): MethodSpec {
    val methodBuilder = MethodSpec
            .methodBuilder(name)
            .addParameters(parameters)
            .addModifiers(modifiers)
            .addAnnotations(annotations)
            .addExceptions(exceptions)
            .varargs(varargs)

    if (returns != null) {
        methodBuilder.returns(returns)
    }

    val code = CodeBlock.builder()
    val builder = MethodCodeBuilder(MethodMeta(name, returns, parameters, modifiers, annotations, varargs), code)
    builder.block()
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
        block: MethodCodeFunc
) = methodSpecInternal(null, name, parameters, modifiers, annotations, throws, varargs, block)

fun methodSpec(
        returns: TypeName,
        name: String,
        parameters: List<ParameterSpec> = emptyList(),
        modifiers: List<Modifier> = emptyList(),
        annotations: List<AnnotationSpec> = emptyList(),
        throws: List<TypeName> = emptyList(),
        varargs: Boolean = false,
        block: MethodCodeFunc
) = methodSpecInternal(returns, name, parameters, modifiers, annotations, throws, varargs, block)

fun methodSpec(
        returns: Type,
        name: String,
        parameters: List<ParameterSpec> = emptyList(),
        modifiers: List<Modifier> = emptyList(),
        annotations: List<AnnotationSpec> = emptyList(),
        throws: List<TypeName> = emptyList(),
        varargs: Boolean = false,
        block: MethodCodeFunc
) = methodSpecInternal(TypeName.get(returns), name, parameters, modifiers, annotations, throws, varargs, block)

fun methodSpec(
        returns: KClass<*>,
        name: String,
        parameters: List<ParameterSpec> = emptyList(),
        modifiers: List<Modifier> = emptyList(),
        annotations: List<AnnotationSpec> = emptyList(),
        throws: List<TypeName> = emptyList(),
        varargs: Boolean = false,
        block: MethodCodeFunc
) = methodSpecInternal(TypeNames.of(returns), name, parameters, modifiers, annotations, throws, varargs, block)

data class MethodMeta(
        val name: String,
        val returns: TypeName?,
        val parameters: List<ParameterSpec>,
        val modifiers: List<Modifier>,
        val annotations: List<AnnotationSpec>,
        val varargs: Boolean
)

class MethodCodeBuilder(val methodMeta: MethodMeta, code: CodeBlock.Builder) : CodeBuilder(code)