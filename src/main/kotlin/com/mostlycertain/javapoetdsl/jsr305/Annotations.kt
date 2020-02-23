package com.mostlycertain.javapoetdsl.jsr305

import com.mostlycertain.javapoetdsl.Annotations

/**
 * Annotations exported by com.google.code.findbugs jsr305 library.
 */
object Annotations {
    val IMMUTABLE = Annotations.of(TypeNames.IMMUTABLE)

    val CHECK_RETURN_VALUE = Annotations.of(TypeNames.CHECK_RETURN_VALUE)

    val PARAMETERS_ARE_NONNULL_BY_DEFAULT = Annotations.of(TypeNames.PARAMETERS_ARE_NONNULL_BY_DEFAULT)

    val NULLABLE = Annotations.of(TypeNames.NULLABLE)

    val NONNULL = Annotations.of(TypeNames.NONNULL)
}