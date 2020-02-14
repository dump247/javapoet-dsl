package com.mostlycertain.javapoetdsl

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

class AnnotationsTest {
    @Test
    fun `generated for java 8`() {
        assertEquals(
                "@javax.annotation.Generated(\"generator.name\")",
                Annotations.generated("generator.name", targetVersion = JavaVersion.VERSION_1_8).toString())
    }

    @Test
    fun `generated for java 8 with date and comments`() {
        assertEquals(
                "@javax.annotation.Generated(value = \"generator.name\", date = \"2019-01-02T03:04:05.789Z\", comments = \"some \\\"comments\\\"\")",
                Annotations.generated(
                        generatorName = "generator.name",
                        generatedAt = Instant.parse("2019-01-02T03:04:05.789Z"),
                        comments = "some \"comments\"",
                        targetVersion = JavaVersion.VERSION_1_8).toString())
    }

    @Test
    fun `generated for java 9`() {
        assertEquals(
                "@javax.annotation.processing.Generated(\"generator.name\")",
                Annotations.generated("generator.name").toString())
    }

    @Test
    fun `generated for java 9 with date and comments`() {
        assertEquals(
                "@javax.annotation.processing.Generated(value = \"generator.name\", date = \"2019-01-02T03:04:05.789Z\", comments = \"some \\\"comments\\\"\")",
                Annotations.generated(
                        generatorName = "generator.name",
                        generatedAt = Instant.parse("2019-01-02T03:04:05.789Z"),
                        comments = "some \"comments\"").toString())
    }

    @Test
    fun `suppressWarnings with no warnings`() {
        assertEquals(
                "@java.lang.SuppressWarnings({})",
                Annotations.suppressWarnings().toString()
        )
    }

    @Test
    fun `suppressWarnings with one warning`() {
        assertEquals(
                "@java.lang.SuppressWarnings(\"unchecked\")",
                Annotations.suppressWarnings("unchecked").toString()
        )
    }

    @Test
    fun `suppressWarnings with multiple warnings`() {
        assertEquals(
                "@java.lang.SuppressWarnings({\"unchecked\", \"some\", \"foo\"})",
                Annotations.suppressWarnings("unchecked", "some", "foo").toString()
        )
    }
}