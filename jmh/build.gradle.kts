import me.champeau.jmh.JMHTask
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
    jmhImplementation(project(":fastcollect"))
    jmhImplementation("it.unimi.dsi:fastutil:8.5.18")
    jmhImplementation("org.korge:korlibs-datastructure:6.1.0")
}

jmh {
    includeTests = false
    verbosity = "EXTRA"
    failOnError = true

    // if a jmhIncludes property is set, forward it to JMH
    findProperty("jmhIncludes")?.also { includes.set(listOf(it as String)) }
}

private fun registerJMHTask(name: String, configuration: JMHTask.()->Unit): TaskProvider<JMHTask> = tasks.register<JMHTask>("jmh$name") {
    group = "jmh"
    description = "Run JMH benchmarks for $name"

    includeTests = false
    verbosity = "EXTRA"
    failOnError = true

    val baseTask = tasks.named<JMHTask>("jmh")

    jmhClasspath = baseTask.get().jmhClasspath
    testRuntimeClasspath = baseTask.get().testRuntimeClasspath
    jarArchive = baseTask.get().jarArchive
    javaLauncher = baseTask.get().javaLauncher
    resultsFile = baseTask.get().resultsFile

    configuration()
}

registerJMHTask("FastCollectList") {
    includes.set(listOf("ListBenchmark\\.fastcollect"))
}

registerJMHTask("FastCollectMap") {
    includes.set(listOf("MapBenchmark\\.fastcollect"))
}

registerJMHTask("FastCollectSet") {
    includes.set(listOf("SetBenchmark\\.fastcollect"))
}

// ensure JMH tasks are never cached
tasks.withType<JMHTask> {
    outputs.upToDateWhen { false }
}
