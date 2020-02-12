package com.mostlycertain.javapoetdsl

import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import java.time.Instant
import java.time.format.DateTimeFormatter

object Annotations {
    val PARAMETERS_ARE_NONNULL_BY_DEFAULT = of(TypeNames.PARAMETERS_ARE_NONNULL_BY_DEFAULT)

    val NULLABLE = of(TypeNames.NULLABLE)

    val NONNULL = of(TypeNames.NONNULL)

    val DEPRECATED = of(TypeNames.DEPRECATED)

    val OVERRIDE = of(TypeNames.OVERRIDE)

    val IMMUTABLE = of(TypeNames.IMMUTABLE)

    val CHECK_RETURN_VALUE = of(TypeNames.CHECK_RETURN_VALUE)

    val SUPPRESS_ALL_WARNINGS = suppressWarnings("all")

    /**
     * Build a [javax.annotation.Generated] annotation.
     *
     * @param generatorName Fully qualified name of the code generator.
     * @param generatedAt Date and time when the code was generated.
     * @param comments Any comments that the code generator may want to include in the generated code.
     */
    fun generated(generatorName: String, generatedAt: Instant? = null, comments: String = ""): AnnotationSpec {
        require(generatorName.isNotBlank())

        val builder = AnnotationSpec
                .builder(TypeNames.GENERATED)
                .addMember("value", "\$S", generatorName)

        if (generatedAt != null) {
            builder.addMember("date", "\$S", DateTimeFormatter.ISO_INSTANT.format(generatedAt))
        }

        if (comments.isNotEmpty()) {
            builder.addMember("comments", "\$S", comments)
        }

        return builder.build()
    }

    fun suppressWarnings(vararg warnings: String): AnnotationSpec = suppressWarnings(warnings.toList())

    fun suppressWarnings(warnings: List<String>): AnnotationSpec {
        val format = if (warnings.size == 1) "\$S" else "{${warnings.joinToString(", ") { "\$S" }}}"

        return AnnotationSpec
                .builder(TypeNames.SUPPRESS_WARNINGS)
                .addMember("value", format, *warnings.toTypedArray())
                .build()
    }

    private fun of(type: ClassName): AnnotationSpec = AnnotationSpec.builder(type).build()

    private fun of(type: ClassName, valueFormat: String, vararg valueArgs: Any): AnnotationSpec {
        return AnnotationSpec.builder(type).addMember("value", valueFormat, *valueArgs).build()
    }
}