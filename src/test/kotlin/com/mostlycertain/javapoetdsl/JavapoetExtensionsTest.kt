package com.mostlycertain.javapoetdsl

import com.mostlycertain.javapoetdsl.TypeNames.types
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeName
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.MalformedURLException
import java.net.URL
import java.net.URLConnection
import javax.lang.model.element.Modifier

class JavapoetExtensionsTest {
    @Test
    fun testToFilePath() {
        assertEquals("Baz.java", ClassName.get("", "Baz").toFilePath().toString())
        assertEquals("com/Baz.java", ClassName.get("com", "Baz").toFilePath().toString())
        assertEquals("com/foo/bar/Baz.java", ClassName.get("com.foo.bar", "Baz").toFilePath().toString())

        assertThrows<IllegalStateException> { ClassName.get("a.b.c", "Class", "Inner").toFilePath() }
    }

    @Test
    fun testWriteToString() {
        val myClass = classSpec(ClassName.get("foo.bar.baz", "MyClass"), listOf(Modifier.PUBLIC, Modifier.FINAL)) {
            fieldDecl(Int::class, "foo", listOf(Modifier.PRIVATE))

            constructorDecl(listOf(parameterSpec(Int::class, "foo")), listOf(Modifier.PUBLIC)) {
                s("this.foo = foo")
            }

            methodDecl("add", listOf(parameterSpec(Int::class, "value")), listOf(Modifier.PUBLIC)) {
                s("this.foo += value")
            }

            methodDecl(
                    returns = String::class,
                    name = "requestData",
                    parameters = listOf(parameterSpec(String::class, "urlStr")),
                    modifiers = listOf(Modifier.PUBLIC),
                    throws = types(IOException::class)
            ) {
                v(StringBuilder::class, "content", e("new StringBuilder()"))
                v(BufferedReader::class, "bufferedReader", literalNull())

                tryDecl {
                    v(URL::class, "url", e("new %T(urlStr)", URL::class))
                    v(URLConnection::class, "urlConnection", e("url.openConnection()"))
                    s("bufferedReader = new %T(new %T(urlConnection.getInputStream())", BufferedReader::class, InputStreamReader::class)
                    v(String::class, "line")

                    whileDecl("(line = bufferedReader.readLine()) != null") {
                        s("content.append(line + %S)", "\n")
                    }
                }.catchDecl(MalformedURLException::class, "ex") {
                    s("throw new %T(urlStr, ex)", IllegalArgumentException::class)
                }.catchDecl(Exception::class, "ex") {
                    s("throw new %T(%S, ex)", IOException::class, "I/O error requesting data")
                }.finallyDecl {
                    ifDecl("bufferedReader != null") {
                        s("bufferedReader.close()")
                    }
                }

                s("return content.toString()")
            }
        }

        val myClassStr = myClass.toJavaFile().writeToString()

        assertEquals("""
            package foo.bar.baz;
            
            import java.io.BufferedReader;
            import java.io.IOException;
            import java.io.InputStreamReader;
            import java.lang.Exception;
            import java.lang.IllegalArgumentException;
            import java.lang.String;
            import java.lang.StringBuilder;
            import java.net.MalformedURLException;
            import java.net.URL;
            import java.net.URLConnection;

            public final class MyClass {
              private int foo;

              public MyClass(final int foo) {
                this.foo = foo;
              }

              public void add(final int value) {
                this.foo += value;
              }

              public String requestData(final String urlStr) throws IOException {
                StringBuilder content = new StringBuilder();
                BufferedReader bufferedReader = null;
                try {
                  URL url = new URL(urlStr);
                  URLConnection urlConnection = url.openConnection();
                  bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream());
                  String line;
                  while ((line = bufferedReader.readLine()) != null) {
                    content.append(line + "\n");
                  }
                } catch (final MalformedURLException ex) {
                  throw new IllegalArgumentException(urlStr, ex);
                } catch (final Exception ex) {
                  throw new IOException("I/O error requesting data", ex);
                } finally {
                  if (bufferedReader != null) {
                    bufferedReader.close();
                  }
                }
                return content.toString();
              }
            }

        """.trimIndent(), myClassStr)
    }

    @Test
    fun `ensureBoxed maps boxed types`() {
        assertEquals(ClassName.get(java.lang.String::class.java), ClassName.get(String::class.java).ensureBoxed())
        assertEquals(ClassName.get(Integer::class.java), TypeName.INT.ensureBoxed())
        assertEquals(ClassName.get(Character::class.java), TypeName.CHAR.ensureBoxed())
    }

    @Test
    fun `ensureUnboxed maps boxed types`() {
        assertEquals(ClassName.get(java.lang.String::class.java), ClassName.get(String::class.java).ensureUnboxed())
        assertEquals(TypeName.INT, ClassName.get(Integer::class.java).ensureUnboxed())
        assertEquals(TypeName.CHAR, ClassName.get(Character::class.java).ensureUnboxed())
    }
}