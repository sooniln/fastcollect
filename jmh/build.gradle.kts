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

private val jmhPow2: Provider<String> = providers.gradleProperty("jmhPow2")
private val jmhLoadFactor: Provider<String> = providers.gradleProperty("jmhLoadFactor")
private val jmhOrder: Provider<String> = providers.gradleProperty("jmhOrder")

tasks.withType<JMHTask> {
    // ensure JMH tasks are never cached
    outputs.cacheIf { false }
    outputs.upToDateWhen { false }

    // ensure jmh tasks cannot run in parallel
    usesService(exclusiveServiceProvider)

    // forward various parameters to JMH
    if (jmhPow2.isPresent) benchmarkParameters.put("pow2", decodeArgs(jmhPow2.get()))
    if (jmhLoadFactor.isPresent) benchmarkParameters.put("loadFactor", decodeArgs(jmhLoadFactor.get()))
    if (jmhOrder.isPresent) benchmarkParameters.put("order", decodeArgs(jmhOrder.get()))
}

private fun decodeArgs(args: String): ListProperty<String> {
    return objects.listProperty(String::class.java).apply { addAll(args.split(",")) }
}
