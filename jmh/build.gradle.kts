import me.champeau.jmh.JMHTask
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.jmh)
}

dependencies {
    jmhImplementation(project(":fastcollect"))
}

private val jmhIncludes: Provider<String> = providers.gradleProperty("jmhIncludes")
private val jmhSize: Provider<String> = providers.gradleProperty("jmhSize")
private val jmhOrder: Provider<String> = providers.gradleProperty("jmhOrder")

jmh {
    includeTests = false
    verbosity = "EXTRA"
    failOnError = true
    resultFormat = "JSON"

    // forward various parameters to JMH
    if (jmhIncludes.isPresent) includes = decodeArgs(jmhIncludes.get())
    if (jmhOrder.isPresent) benchmarkParameters.put("order", decodeArgs(jmhOrder.get()))
    if (jmhSize.isPresent) benchmarkParameters.put("size", decodeArgs(jmhSize.get()))
}

private val copyTask = tasks.register<Copy>("copyJmhResults") {
    group = "benchmark"
    description = "Copy last JMH results into benchmark-results directory."

    from("build/results/jmh/results.json")
    into("benchmark-results")
    rename { "${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))}.json" }
}

private abstract class ExclusiveTaskService : BuildService<BuildServiceParameters.None>
private val exclusiveServiceProvider = gradle.sharedServices.registerIfAbsent("exclusiveTask", ExclusiveTaskService::class.java) {
    maxParallelUsages = 1
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

private fun decodeArgs(args: String): ListProperty<String> =
    objects.listProperty(String::class.java).apply { addAll(args.split(",")) }
