import org.gradle.kotlin.dsl.kotlin

plugins {
    kotlin("jvm")
    alias(libs.plugins.jmh)
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    jmhImplementation(project(":"))
    jmhImplementation("it.unimi.dsi:fastutil:8.5.18")
}

jmh {
    includeTests = false
    verbosity = "EXTRA"
    failOnError = true
    //includes.add("Int2IntMapBenchmark\\.fastcollect(Get|Put)")
    includes.add("IntArrayIndexOfBenchmark.fast")
}

tasks.withType<me.champeau.jmh.JMHTask> {
    outputs.upToDateWhen { false }
}
