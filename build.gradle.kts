plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.mavenPublish) apply false
}

private val jarSizesFile = layout.projectDirectory.file("fastcollect/api/jar-sizes.txt")

private val jarSizeLines = objects.listProperty<String>()

private val jarSizesContent = jarSizeLines.map { lines ->
    lines.sorted().joinToString("\n", postfix = "\n")
}

// jvmJar/shadowJar etc. are registered by their plugins only after each subproject finishes
// evaluating (including its own afterEvaluate hooks), so the tracked-jar set can only be
// determined once every project in the build has been evaluated.
gradle.projectsEvaluated {
    val trackedJars: List<Jar> = (
        project(":fastcollect").tasks.withType<Jar>() +
            project(":fastcollect-java").tasks.withType<Jar>()
        ).filter { it.enabled && it.archiveClassifier.orNull.isNullOrEmpty() }

    trackedJars.forEach { jar ->
        jarSizeLines.add(jar.archiveFileName.zip(jar.archiveFile) { name, file -> "$name ${file.asFile.length()}" })
    }

    tasks.named("updateJarSizes") { dependsOn(trackedJars) }
    tasks.named("checkJarSizes") { dependsOn(trackedJars) }
    project(":fastcollect").tasks.named("check") { dependsOn(tasks.named("checkJarSizes")) }
}

tasks.register("updateJarSizes") {
    group = "verification"
    description = "Writes the sizes of all published jar artifacts to $jarSizesFile."

    // captured into locals so the action stays configuration-cache-safe
    val content = jarSizesContent
    val outputFile = jarSizesFile.asFile
    doLast {
        outputFile.writeText(content.get())
    }
}

tasks.register("checkJarSizes") {
    group = "verification"
    description = "Fails if $jarSizesFile is out of date."

    val content = jarSizesContent
    val outputFile = jarSizesFile.asFile
    doLast {
        val expected = if (outputFile.exists()) outputFile.readText() else ""
        check(content.get() == expected) {
            "$outputFile is out of date; run ./gradlew updateJarSizes to refresh it."
        }
    }
}
