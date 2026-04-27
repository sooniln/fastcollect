import org.gradle.kotlin.dsl.kotlin

plugins {
    kotlin("jvm")
    alias(libs.plugins.jmh)
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    jmhImplementation(project(":"))
    jmhImplementation("it.unimi.dsi:fastutil:8.5.18")
}

jmh {
    includeTests = false
    verbosity = "EXTRA"
    failOnError = true
    //profilers.add("gc")
    includes.add("Int2IntMapBenchmark\\.fastcollectIter")
}

tasks.withType<me.champeau.jmh.JMHTask> {
    outputs.upToDateWhen { false }
}
