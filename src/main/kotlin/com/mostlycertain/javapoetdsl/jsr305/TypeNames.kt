package com.mostlycertain.javapoetdsl.jsr305

import com.mostlycertain.javapoetdsl.TypeNames.className

object TypeNames {
    val PARAMETERS_ARE_NONNULL_BY_DEFAULT = className(
            "javax.annotation",
            "ParametersAreNonnullByDefault")

    val NULLABLE = className("javax.annotation", "Nullable")

    val NONNULL = className("javax.annotation", "Nonnull")

    val IMMUTABLE = className("javax.annotation.concurrent", "Immutable")

    val CHECK_RETURN_VALUE = className("javax.annotation", "CheckReturnValue")
}