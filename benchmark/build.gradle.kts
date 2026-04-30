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
}

private fun registerJMHTask(name: String, configuration: JMHTask.()->Unit): TaskProvider<JMHTask> = tasks.register<JMHTask>(name) {
    group = "verification"

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

registerJMHTask("jmhFastCollectList") {
    description = "Run JMH benchmarks for FastCollect IntArrayDeque"
    includes.add("ListBenchmark\\.fastcollect")
}

registerJMHTask("jmhFastCollectMap") {
    description = "Run JMH benchmarks for FastCollect Int2IntHashMap"
    includes.add("MapBenchmark\\.fastcollect")
}

registerJMHTask("jmhFastCollectSet") {
    description = "Run JMH benchmarks for FastCollect IntHashSet"
    includes.add("SetBenchmark\\.fastcollect")
}

// ensure JMH tasks are never cached
tasks.withType<JMHTask> {
    outputs.upToDateWhen { false }
}
