import me.champeau.jmh.JMHTask
import org.gradle.kotlin.dsl.kotlin

plugins {
    kotlin("jvm")
    alias(libs.plugins.jmh)
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    jmhImplementation(project(":fastcollect"))
}

jmh {
    includeTests = false
    verbosity = "EXTRA"
    failOnError = true
    resultFormat = "JSON"

    // if a jmhIncludes property is set, forward it to JMH
    findProperty("jmhIncludes")?.also { includes.set(listOf(it as String)) }
}

registerJMHTask("IntList") {
    includes.set(listOf("IntListBenchmark\\."))
}

registerJMHTask("LongList") {
    includes.set(listOf("LongListBenchmark\\."))
}

registerJMHTask("IntSet") {
    includes.set(listOf("IntSetBenchmark\\."))
}

registerJMHTask("LongSet") {
    includes.set(listOf("LongSetBenchmark\\."))
}

registerJMHTask("IntMap") {
    includes.set(listOf("IntMapBenchmark\\."))
}

registerJMHTask("LongMap") {
    includes.set(listOf("LongMapBenchmark\\."))
}

private fun registerJMHTask(name: String, configuration: JMHTask.()->Unit): TaskProvider<JMHTask> = tasks.register<JMHTask>("jmh$name") {
    group = "benchmark"
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

    // forward various parameters to JMH
    findProperty("jmhSize")?.also { prop -> benchmarkParameters.put("size", objects.listProperty(String::class.java).apply { addAll(prop.toString().split(",")) }) }
    findProperty("jmhType")?.also { prop -> benchmarkParameters.put("type", objects.listProperty(String::class.java).apply { addAll(prop.toString().split(",")) }) }
    findProperty("jmhOrder")?.also { prop -> benchmarkParameters.put("order", objects.listProperty(String::class.java).apply { addAll(prop.toString().split(",")) }) }
}
