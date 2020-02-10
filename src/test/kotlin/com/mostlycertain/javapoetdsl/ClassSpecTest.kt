package com.mostlycertain.javapoetdsl

import com.squareup.javapoet.ClassName
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.MalformedURLException
import java.net.URL
import java.net.URLConnection
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.Modifier.FINAL
import javax.lang.model.element.Modifier.PRIVATE

class ClassSpecTest {
    @Test
    fun `toString with example in readme`() {
        val myClass = classSpec("MyClass", listOf(PUBLIC, FINAL)) {
            fieldDecl(Int::class, "foo", listOf(PRIVATE))

            constructorDecl(listOf(parameterSpec(Int::class, "foo")), listOf(PUBLIC)) {
                s("this.foo = foo")
            }

            methodDecl("add", listOf(parameterSpec(Int::class, "value")), listOf(PUBLIC)) {
                s("this.foo += value")
            }

            methodDecl(
                    returns = String::class,
                    name = "requestData",
                    parameters = listOf(parameterSpec(String::class, "urlStr")),
                    modifiers = listOf(PUBLIC),
                    exceptions = typeList(IOException::class)
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

        assertEquals("""
            public final class MyClass {
              private int foo;

              public MyClass(final int foo) {
                this.foo = foo;
              }

              public void add(final int value) {
                this.foo += value;
              }

              public java.lang.String requestData(final java.lang.String urlStr) throws java.io.IOException {
                java.lang.StringBuilder content = new StringBuilder();
                java.io.BufferedReader bufferedReader = null;
                try {
                  java.net.URL url = new java.net.URL(urlStr);
                  java.net.URLConnection urlConnection = url.openConnection();
                  bufferedReader = new java.io.BufferedReader(new java.io.InputStreamReader(urlConnection.getInputStream());
                  java.lang.String line;
                  while ((line = bufferedReader.readLine()) != null) {
                    content.append(line + "\n");
                  }
                } catch (final java.net.MalformedURLException ex) {
                  throw new java.lang.IllegalArgumentException(urlStr, ex);
                } catch (final java.lang.Exception ex) {
                  throw new java.io.IOException("I/O error requesting data", ex);
                } finally {
                  if (bufferedReader != null) {
                    bufferedReader.close();
                  }
                }
                return content.toString();
              }
            }

        """.trimIndent(), myClass.toString())
    }

    @Test
    fun `toString with class package`() {
        val myClass = classSpec(ClassName.get("a.b.c", "MyClass")) {
            fieldDecl(String::class, "fieldName")
            fieldDecl(InputStream::class, "stream")
        }

        assertEquals("""
            package a.b.c;
            
            class MyClass {
              java.lang.String fieldName;

              java.io.InputStream stream;
            }

        """.trimIndent(), myClass.toString())
    }

    @Test
    fun `toJavaFile with defaults`() {
        val myClass = classSpec(ClassName.get("a.b.c", "MyClass")) {
            fieldDecl(String::class, "fieldName")
            fieldDecl(InputStream::class, "stream")
        }

        assertEquals("""
            package a.b.c;

            import java.io.InputStream;
            import java.lang.String;

            class MyClass {
              String fieldName;

              InputStream stream;
            }

        """.trimIndent(), myClass.toJavaFile().writeToString())
    }

    @Test
    fun `toJavaFile with skip java lang imports`() {
        val myClass = classSpec(ClassName.get("a.b.c", "MyClass")) {
            fieldDecl(String::class, "fieldName")
            fieldDecl(InputStream::class, "stream")
        }

        assertEquals("""
            package a.b.c;
            
            import java.io.InputStream;

            class MyClass {
              String fieldName;

              InputStream stream;
            }

        """.trimIndent(), myClass.toJavaFile(skipJavaLangImports = true).writeToString())
    }
}