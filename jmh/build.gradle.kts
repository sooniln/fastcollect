import me.champeau.jmh.JMHTask
import org.gradle.kotlin.dsl.kotlin
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    kotlin("jvm")
    alias(libs.plugins.jmh)
}

repositories {
    mavenCentral()
}

dependencies {
    jmhImplementation(project(":fastcollect"))
    jmhImplementation("it.unimi.dsi:fastutil:8.5.18")
    jmhImplementation("org.korge:korlibs-datastructure:6.1.0")
    jmhImplementation("io.github.bluuewhale:hashsmith:0.2.0")
    jmhImplementation("org.eclipse.collections:eclipse-collections:13.0.0")
}

jmh {
    includeTests = false
    verbosity = "EXTRA"
    failOnError = true
    resultFormat = "JSON"

    // if a jmhIncludes property is set, forward it to JMH
    findProperty("jmhIncludes")?.also { includes.set(listOf(it as String)) }
}

private fun registerJMHTask(name: String, configuration: JMHTask.()->Unit): TaskProvider<JMHTask> = tasks.register<JMHTask>("jmh$name") {
    group = "jmh"
    description = "Run JMH benchmarks for $name"

    includeTests = false
    verbosity = "EXTRA"
    failOnError = true
    resultFormat = "JSON"

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

registerJMHTask("List") {
    includes.set(listOf("ListBenchmark\\."))
}

registerJMHTask("Map") {
    includes.set(listOf("MapBenchmark\\."))
}

registerJMHTask("Set") {
    includes.set(listOf("SetBenchmark\\."))
}

private val copyTask = tasks.register<Copy>("CopyJmhResults") {
    description = "Copy last JMH results into benchmark-results directory."

    from("build/results/jmh/results.json")
    into("benchmark-results")
    rename { "${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))}.json" }
}

private abstract class ExclusiveTaskService : BuildService<BuildServiceParameters.None>
private val exclusiveServiceProvider = gradle.sharedServices.registerIfAbsent("exclusiveTask", ExclusiveTaskService::class.java) {
    maxParallelUsages.set(1)
}

tasks.withType<JMHTask> {
    // ensure JMH tasks are never cached
    outputs.cacheIf { false }
    outputs.upToDateWhen { false }

    // ensure jmh tasks cannot run in parallel
    usesService(exclusiveServiceProvider)

    // save all benchmark data
    finalizedBy(copyTask)
}
