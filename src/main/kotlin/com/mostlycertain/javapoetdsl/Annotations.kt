package com.mostlycertain.javapoetdsl

import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import java.time.Instant
import java.time.format.DateTimeFormatter

object Annotations {
    val DEPRECATED = of(TypeNames.DEPRECATED)

    val OVERRIDE = of(TypeNames.OVERRIDE)

    val FUNCTIONAL_INTERFACE = of(TypeNames.FUNCTIONAL_INTERFACE)

    val SAFE_VARARGS = of(TypeNames.SAFE_VARARGS)

    val SUPPRESS_ALL_WARNINGS = suppressWarnings("all")

    fun suppressWarnings(vararg warnings: String): AnnotationSpec = suppressWarnings(warnings.toList())

    fun suppressWarnings(warnings: List<String>): AnnotationSpec {
        val format = if (warnings.size == 1) "\$S" else "{${warnings.joinToString(", ") { "\$S" }}}"

        return AnnotationSpec
                .builder(TypeNames.SUPPRESS_WARNINGS)
                .addMember("value", format, *warnings.toTypedArray())
                .build()
    }

    fun of(type: ClassName): AnnotationSpec = AnnotationSpec.builder(type).build()

    /**
     * Build an annotation that marks a type as generated.
     *
     * The resulting annotation is
     * [javax.annotation.Generated](https://docs.oracle.com/javase/8/docs/api/javax/annotation/Generated.html)
     * for java versions <= 8 and
     * [javax.annotation.processing.Generated](https://docs.oracle.com/javase/9/docs/api/javax/annotation/processing/Generated.html)
     * for java versions >= 9.
     *
     * @param generatorName Fully qualified name of the code generator.
     * @param generatedAt Date and time when the code was generated.
     * @param comments Any comments that the code generator may want to include in the generated code.
     * @param targetVersion Java version to generate the annotation for
     */
    fun generated(
            generatorName: String,
            generatedAt: Instant? = null,
            comments: String = "",
            targetVersion: JavaVersion = JavaVersion.VERSION_1_9
    ): AnnotationSpec {
        require(generatorName.isNotBlank())

        val builder = AnnotationSpec
                .builder(if (targetVersion.majorVersion <= 8) TypeNames.GENERATED_JDK8 else TypeNames.GENERATED_JDK9)
                .addMember("value", "\$S", generatorName)

        if (generatedAt != null) {
            builder.addMember("date", "\$S", DateTimeFormatter.ISO_INSTANT.format(generatedAt))
        }

        if (comments.isNotEmpty()) {
            builder.addMember("comments", "\$S", comments)
        }

        return builder.build()
    }
}