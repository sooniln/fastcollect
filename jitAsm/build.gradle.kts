import org.gradle.kotlin.dsl.java

plugins {
    java
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

dependencies {
    implementation(project(":fastcollect"))
    implementation("it.unimi.dsi:fastutil:8.5.18")
}

private fun registerJitAsm(name: String, configuration: JavaExec.() -> Unit): TaskProvider<JavaExec> = tasks.register<JavaExec>(name) {
    group = "verification"
    description = "Run a small harness that heats up and prints the generated machine code (requires hsdis to be present on path)."

    classpath = sourceSets["main"].runtimeClasspath

    jvmArgs(
        "-Xms256m",
        "-Xmx256m",
        "-Xbatch",
        // Force C2 (server compiler) so the assembly reflects final hot-path optimizations.
        "-XX:-TieredCompilation",
        "-XX:CICompilerCount=2",
        "-XX:CompileThreshold=1000",
        // Assembly
        "-XX:+UnlockDiagnosticVMOptions",
        "-XX:+PrintAssembly",
        "-XX:PrintAssemblyOptions=intel",
    )

    configuration()
}

registerJitAsm("jitAsmIntHashSetContains") {
    mainClass = "io.github.sooniln.fastcollect.IntHashSetContainsAsmProbe"
    jvmArgs(
        "-XX:CompileCommand=quiet",
        "-XX:CompileCommand=compileonly,io.github.sooniln.fastcollect.ints.IntHashSet::contains",
        "-XX:CompileCommand=print,io.github.sooniln.fastcollect.ints.IntHashSet::contains",
    )
}

registerJitAsm("jitAsmIntArrayDequeIterate") {
    mainClass = "io.github.sooniln.fastcollect.IntArrayDequeIterateAsmProbe"
    jvmArgs(
        "-XX:CompileCommand=quiet",
        "-XX:CompileCommand=compileonly,io.github.sooniln.fastcollect.IntArrayDequeIterateAsmProbe::iterateFastCollect",
        "-XX:CompileCommand=print,io.github.sooniln.fastcollect.IntArrayDequeIterateAsmProbe::iterateFastCollect",
        "-XX:CompileCommand=compileonly,io.github.sooniln.fastcollect.IntArrayDequeIterateAsmProbe::iterateFastutil",
        "-XX:CompileCommand=print,io.github.sooniln.fastcollect.IntArrayDequeIterateAsmProbe::iterateFastutil",
    )
}
