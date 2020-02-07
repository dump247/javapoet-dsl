package com.mostlycertain.javapoetdsl

import com.squareup.javapoet.ClassName
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.MalformedURLException
import java.net.URL
import java.net.URLConnection
import javax.lang.model.element.Modifier

class JavaFileTest {
    @Test
    fun testJavaFilePath() {
        assertEquals("Baz.java", javaFilePath(ClassName.get("", "Baz")).toString())
        assertEquals("com/foo/bar/Baz.java", javaFilePath(ClassName.get("com.foo.bar", "Baz")).toString())
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

        val myClassStr = javaFileBuilder(myClass)
                .skipJavaLangImports(true)
                .build()
                .writeToString()

        assertEquals("""
            package foo.bar.baz;
            
            import java.io.BufferedReader;
            import java.io.IOException;
            import java.io.InputStreamReader;
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
}