import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "1.3.61"

    // Kotlin documentation engine
    id("org.jetbrains.dokka") version "0.10.1"

    // Apply the java-library plugin for API and implementation separation.
    `java-library`

    `maven-publish`
}

repositories {
    jcenter()
}

dependencies {
    api("com.squareup:javapoet:1.12.1")

    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // JUnit: unit test framework
    testImplementation("org.junit.jupiter:junit-jupiter:5.6.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.dokka {
    outputFormat = "html"
    outputDirectory = "$buildDir/javadoc"

    configuration {
        jdkVersion = 8
    }
}

val dokkaJar by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    archiveClassifier.set("javadoc")
    from(tasks.dokka)
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

publishing {
    publications {
        create<MavenPublication>("default") {
            pom {
                name.set("JavaPoet DSL")
                description.set("Kotlin DSL wrapper for the JavaPoet Java code generation library")
                url.set("https://github.com/dump247/javapoet-dsl")
                licenses {
                    license {
                        name.set("MIT")
                        url.set("http://www.opensource.org/licenses/mit-license.php")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/dump247/javapoet-dsl.git")
                    developerConnection.set("scm:git:git@github.com:dump247/javapoet-dsl.git")
                    url.set("https://github.com/dump247/javapoet-dsl")
                }
            }

            from(components["java"])
            artifact(sourcesJar.get())
            artifact(dokkaJar)
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/dump247/javapoet-dsl")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}