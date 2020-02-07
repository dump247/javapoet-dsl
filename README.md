# What is JavaPoet?

[JavaPoet](https://github.com/square/javapoet) is a Java API for generating `.java`
source files. JavaPoet is developed by [Square](https://squareup.com). The JavaPoet
library does a great job of outputting valid java code.

# What is JavaPoet DSL?

This project is a DSL written in Kotlin on top of the JavaPoet API. The JavaPoet
API is composed of a set of fluent builder interfaces. While this works quite nicely,
the result can be very difficult to read and understand.

## Differences from JavaPoet

Unlike JavaPoet, this DSL uses percent signs (`%`) instead of dollar signs (`$`) for formatting markers.
This makes it easier to write format strings since Kotlin uses the dollar sign for string
interpolation.

The DSL makes use of `this` context objects to define code blocks.

# Examples

## Code Snippet

```kotlin
val variableName = "counter"

val block = codeBlock {
    v(Int::class, variableName, literal(1))
    
    whileDecl("%L < 10", variableName) {
        ifDecl("%L %% 2 == 0", variableName) {
            s("%T.out.printf(%S, %L)", System::class, "even: %d", variableName)
        }.elseDecl {
            s("%T.out.printf(%S, %L)", System::class, "odd: %d", variableName)
        }

        s("%L += 1", variableName)
    }
}

println(block)

// -- Output --
// int counter = 1;
// while (counter < 10) {
//  if (counter % 2 == 0) {
//    java.lang.System.out.printf("even: %d\n", counter);
//  } else {
//    java.lang.System.out.printf("odd: %d\n", counter);
//  }
//  counter += 1;
// }
```

## Class

```kotlin
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.MalformedURLException
import java.net.URL
import java.net.URLConnection
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.Modifier.FINAL
import javax.lang.model.element.Modifier.PRIVATE

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

println(myClass)

// -- Output --
// public final class MyClass {
//   private int foo;
// 
//   public MyClass(final int foo) {
//     this.foo = foo;
//   }
// 
//   public void add(final int value) {
//     this.foo += value;
//   }
// 
//   public java.lang.String requestData(final java.lang.String urlStr) throws java.io.IOException {
//     java.lang.StringBuilder content = new StringBuilder();
//     java.io.BufferedReader bufferedReader = null;
//     try {
//       java.net.URL url = new java.net.URL(urlStr);
//       java.net.URLConnection urlConnection = url.openConnection();
//       bufferedReader = new java.io.BufferedReader(new java.io.InputStreamReader(urlConnection.getInputStream());
//       java.lang.String line;
//       while ((line = bufferedReader.readLine()) != null) {
//         content.append(line + "\n");
//       }
//     } catch (final java.net.MalformedURLException ex) {
//       throw new java.lang.IllegalArgumentException(urlStr, ex);
//     } catch (final java.lang.Exception ex) {
//       throw new java.io.IOException("I/O error requesting data", ex);
//     } finally {
//       if (bufferedReader != null) {
//         bufferedReader.close();
//       }
//     }
//     return content.toString();
//   }
// }
```