package com.mostlycertain.javapoetdsl

import com.mostlycertain.javapoetdsl.TypeNames.className
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import java.lang.reflect.Type
import java.nio.file.Path
import javax.lang.model.element.Modifier
import kotlin.reflect.KClass

typealias ClassSpecFunc = ClassSpecBuilder.() -> Unit

class ClassSpec(val className: ClassName, val typeSpec: TypeSpec) {
    override fun toString(): String {
        return if (className.packageName().isEmpty()) {
            typeSpec.toString()
        } else {
            "package ${className.packageName()};\n\n${typeSpec}"
        }
    }

    val filePath: Path
        get() = className.toFilePath()

    fun toJavaFile(
            skipJavaLangImports: Boolean = false
    ): JavaFile {
        if (className.isNested) {
            throw IllegalStateException("Can not generate a java file for an inner class: $className")
        }

        return JavaFile.builder(className.packageName(), typeSpec)
                .skipJavaLangImports(skipJavaLangImports)
                .build()
    }
}

fun classSpec(
        name: ClassName,
        modifiers: List<Modifier> = emptyList(),
        annotations: List<AnnotationSpec> = emptyList(),
        superInterfaces: List<TypeName> = emptyList(),
        superClass: TypeName? = null,
        block: ClassSpecFunc
): ClassSpec {
    val classBuilder = TypeSpec.classBuilder(name)
            .addModifiers(*modifiers.toTypedArray()) // no Iterable overload
            .addAnnotations(annotations)
            .addSuperinterfaces(superInterfaces)

    if (superClass != null) {
        classBuilder.superclass(superClass)
    }

    ClassSpecBuilder(ClassMeta(name, annotations, modifiers), classBuilder).block()

    return ClassSpec(name, classBuilder.build())
}

fun classSpec(
        name: String,
        modifiers: List<Modifier> = emptyList(),
        annotations: List<AnnotationSpec> = emptyList(),
        superInterfaces: List<TypeName> = emptyList(),
        superClass: TypeName? = null,
        block: ClassSpecFunc
) = classSpec(className("", name), modifiers, annotations, superInterfaces, superClass, block)

data class ClassMeta(
        val name: ClassName,
        val annotations: List<AnnotationSpec>,
        val modifiers: List<Modifier>
)

class ClassSpecBuilder(
        val classMeta: ClassMeta,
        private val spec: TypeSpec.Builder
) {
    fun fieldDecl(
            type: TypeName,
            name: String,
            modifiers: List<Modifier> = emptyList(),
            annotations: List<AnnotationSpec> = emptyList(),
            initializer: CodeExpression? = null
    ): FieldSpec {
        val fieldSpec = fieldSpec(type, name, modifiers, annotations, initializer)
        spec.addField(fieldSpec)
        return fieldSpec
    }

    fun fieldDecl(
            type: Type,
            name: String,
            modifiers: List<Modifier> = emptyList(),
            annotations: List<AnnotationSpec> = emptyList(),
            initializer: CodeExpression? = null
    ): FieldSpec {
        val fieldSpec = fieldSpec(type, name, modifiers, annotations, initializer)
        spec.addField(fieldSpec)
        return fieldSpec
    }

    fun fieldDecl(
            type: KClass<*>,
            name: String,
            modifiers: List<Modifier> = emptyList(),
            annotations: List<AnnotationSpec> = emptyList(),
            initializer: CodeExpression? = null
    ): FieldSpec {
        val fieldSpec = fieldSpec(type, name, modifiers, annotations, initializer)
        spec.addField(fieldSpec)
        return fieldSpec
    }

    fun classDecl(
            name: String,
            modifiers: List<Modifier> = emptyList(),
            annotations: List<AnnotationSpec> = emptyList(),
            superInterfaces: List<TypeName> = emptyList(),
            superClass: TypeName? = null,
            block: ClassSpecFunc
    ): ClassSpec {
        val classSpec = classSpec(classMeta.name.nestedClass(name), modifiers, annotations, superInterfaces, superClass, block)
        spec.addType(classSpec.typeSpec)
        return classSpec
    }

    fun classDecl(
            name: ClassName,
            modifiers: List<Modifier> = emptyList(),
            annotations: List<AnnotationSpec> = emptyList(),
            superInterfaces: List<TypeName> = emptyList(),
            superClass: TypeName? = null,
            block: ClassSpecFunc
    ): ClassSpec {
        check(classMeta.name == name.enclosingClassName())

        val classSpec = classSpec(name, modifiers, annotations, superInterfaces, superClass, block)
        spec.addType(classSpec.typeSpec)
        return classSpec
    }

    fun methodDecl(
            name: String,
            parameters: List<ParameterSpec> = emptyList(),
            modifiers: List<Modifier> = emptyList(),
            annotations: List<AnnotationSpec> = emptyList(),
            throws: List<TypeName> = emptyList(),
            varargs: Boolean = false,
            body: MethodCodeFunc? = null
    ): MethodSpec {
        val methodSpec = methodSpec(name, parameters, modifiers, annotations, throws, varargs, body)
        spec.addMethod(methodSpec)
        return methodSpec
    }

    fun methodDecl(
            returns: TypeName,
            name: String,
            parameters: List<ParameterSpec> = emptyList(),
            modifiers: List<Modifier> = emptyList(),
            annotations: List<AnnotationSpec> = emptyList(),
            throws: List<TypeName> = emptyList(),
            varargs: Boolean = false,
            body: MethodCodeFunc? = null
    ): MethodSpec {
        val methodSpec = methodSpec(returns, name, parameters, modifiers, annotations, throws, varargs, body)
        spec.addMethod(methodSpec)
        return methodSpec
    }

    fun methodDecl(
            returns: Type,
            name: String,
            parameters: List<ParameterSpec> = emptyList(),
            modifiers: List<Modifier> = emptyList(),
            annotations: List<AnnotationSpec> = emptyList(),
            throws: List<TypeName> = emptyList(),
            varargs: Boolean = false,
            body: MethodCodeFunc? = null
    ): MethodSpec {
        val methodSpec = methodSpec(returns, name, parameters, modifiers, annotations, throws, varargs, body)
        spec.addMethod(methodSpec)
        return methodSpec
    }

    fun methodDecl(
            returns: KClass<*>,
            name: String,
            parameters: List<ParameterSpec> = emptyList(),
            modifiers: List<Modifier> = emptyList(),
            annotations: List<AnnotationSpec> = emptyList(),
            throws: List<TypeName> = emptyList(),
            varargs: Boolean = false,
            body: MethodCodeFunc? = null
    ): MethodSpec {
        val methodSpec = methodSpec(returns, name, parameters, modifiers, annotations, throws, varargs, body)
        spec.addMethod(methodSpec)
        return methodSpec
    }

    fun constructorDecl(
            parameters: List<ParameterSpec> = emptyList(),
            modifiers: List<Modifier> = emptyList(),
            annotations: List<AnnotationSpec> = emptyList(),
            throws: List<TypeName> = emptyList(),
            varargs: Boolean = false,
            body: ConstructorCodeFunc
    ): MethodSpec {
        val constructorSpec = constructorSpec(parameters, modifiers, annotations, throws, varargs, body)
        spec.addMethod(constructorSpec)
        return constructorSpec
    }

    fun initializerBlockDecl(block: CodeFunc) {
        val code = CodeBlock.builder()
        buildBlock(code, block)
        spec.addInitializerBlock(code.build())
    }

    fun staticInitializerBlockDecl(block: CodeFunc) {
        val code = CodeBlock.builder()
        buildBlock(code, block)
        spec.addStaticBlock(code.build())
    }
}