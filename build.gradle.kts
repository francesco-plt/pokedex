import net.ltgt.gradle.errorprone.errorprone
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.*
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    java
    jacoco
    id("org.springframework.boot") version "3.5.14"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.diffplug.spotless") version "7.2.1"
    id("net.ltgt.errorprone") version "4.3.0"
    id("org.owasp.dependencycheck") version "12.1.8"
}

group = "com.pokedex"
version = "0.1.0-SNAPSHOT"
description = "Minimal Spring Boot starter with hello-world endpoint"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2025.0.0")
    }
}

val sourceSets = the<SourceSetContainer>()
val test by sourceSets.getting {
    java.setSrcDirs(listOf("src/test/unitTest/java"))
    resources.setSrcDirs(listOf("src/test/unitTest/resources"))
}
val integrationTest by sourceSets.creating {
    java.srcDir("src/test/integrationTest/java")
    resources.srcDir("src/test/integrationTest/resources")
    compileClasspath += sourceSets["main"].output + configurations["testRuntimeClasspath"]
    runtimeClasspath += output + compileClasspath
}
val architectureTest by sourceSets.creating {
    java.srcDir("src/test/architectureTest/java")
    resources.srcDir("src/test/architectureTest/resources")
    compileClasspath += sourceSets["main"].output + configurations["testRuntimeClasspath"]
    runtimeClasspath += output + compileClasspath
}

configurations {
    named(integrationTest.implementationConfigurationName) {
        extendsFrom(configurations.testImplementation.get())
    }
    named(integrationTest.runtimeOnlyConfigurationName) {
        extendsFrom(configurations.testRuntimeOnly.get())
    }
    named(architectureTest.implementationConfigurationName) {
        extendsFrom(configurations.testImplementation.get())
    }
    named(architectureTest.runtimeOnlyConfigurationName) {
        extendsFrom(configurations.testRuntimeOnly.get())
    }
}

dependencyLocking {
    lockAllConfigurations()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    implementation("io.github.openfeign:feign-okhttp")
    implementation("com.squareup.okhttp3:okhttp:5.4.0")
    implementation("org.slf4j:slf4j-api")
    implementation("org.apache.commons:commons-lang3:3.20.0")

    compileOnly("org.projectlombok:lombok:1.18.46")
    annotationProcessor("org.projectlombok:lombok:1.18.46")
    testCompileOnly("org.projectlombok:lombok:1.18.46")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.46")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.squareup.okhttp3:mockwebserver:5.4.0")

    errorprone("com.google.errorprone:error_prone_core:2.43.0")
    errorprone("com.uber.nullaway:nullaway:0.12.9")
    compileOnly("org.jspecify:jspecify:1.0.0")
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(25)
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-parameters", "-Xlint:all"))

    options.errorprone {
        disable("EqualsGetClass")
        error("NullAway")
        option("NullAway:AnnotatedPackages", "com.pokedex.app")
    }

    if (name.contains("test", ignoreCase = true)) {
        options.errorprone {
            disable("NullAway")
        }
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

val jacocoExcludes =
    listOf(
        "**/PokeApiClientConfig.class",
        "**/FunTranslationsClientConfig.class",
    )

tasks.named<JacocoReport>("jacocoTestReport") {
    executionData.setFrom(
        fileTree(layout.buildDirectory.get().asFile) {
            include(
                "jacoco/test.exec",
                "jacoco/integrationTest.exec",
                "jacoco/architectureTest.exec",
            )
        }
    )
    classDirectories.setFrom(
        sourceSets["main"].output.asFileTree.matching {
            exclude(jacocoExcludes)
        }
    )
}

tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    executionData.setFrom(
        fileTree(layout.buildDirectory.get().asFile) {
            include(
                "jacoco/test.exec",
                "jacoco/integrationTest.exec",
                "jacoco/architectureTest.exec",
            )
        }
    )
    classDirectories.setFrom(
        sourceSets["main"].output.asFileTree.matching {
            exclude(jacocoExcludes)
        }
    )
    violationRules {
        rule {
            limit {
                counter = "INSTRUCTION"
                value = "COVEREDRATIO"
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}

val integrationTestTask = tasks.register<Test>("integrationTest") {
    description = "Runs integration tests."
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    testClassesDirs = integrationTest.output.classesDirs
    classpath = integrationTest.runtimeClasspath
    shouldRunAfter(tasks.test)
}

val architectureTestTask = tasks.register<Test>("architectureTest") {
    description = "Runs architecture tests."
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    testClassesDirs = architectureTest.output.classesDirs
    classpath = architectureTest.runtimeClasspath
    shouldRunAfter(tasks.test)
}

tasks.check {
    dependsOn(integrationTestTask)
    dependsOn(architectureTestTask)
    dependsOn("spotlessCheck")
}

tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

spotless {
    java {
        target("src/**/*.java")
        trimTrailingWhitespace()
        endWithNewline()
    }
    format("misc") {
        target("*.md", "*.yml", "*.yaml", "*.properties", "*.kts", ".gitignore", ".gitattributes", ".env.example")
        trimTrailingWhitespace()
        endWithNewline()
    }
}

dependencyCheck {
    failBuildOnCVSS = 7.0f
    suppressionFile = "dependency-check-suppressions.xml"
    analyzers.assemblyEnabled = false
}
