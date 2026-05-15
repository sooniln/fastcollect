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
    implementation("org.eclipse.collections:eclipse-collections:13.0.0")
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

registerJitAsm("jitAsmIntHashSetContains") {
    mainClass = "io.github.sooniln.fastcollect.IntHashSetContainsAsmProbe"
    jvmArgs(
        "-XX:CompileCommand=quiet",
        "-XX:CompileCommand=compileonly,io.github.sooniln.fastcollect.ints.IntHashSet::contains",
        "-XX:CompileCommand=print,io.github.sooniln.fastcollect.ints.IntHashSet::contains",
        "-XX:CompileCommand=compileonly,org.eclipse.collections.impl.set.mutable.primitive.IntHashSet::contains",
        "-XX:CompileCommand=print,org.eclipse.collections.impl.set.mutable.primitive.IntHashSet::contains",
    )
}

registerJitAsm("jitAsmIntHashSetGrow") {
    mainClass = "io.github.sooniln.fastcollect.IntHashSetGrowAsmProbe"
    jvmArgs(
        "-XX:CompileCommand=quiet",
        "-XX:CompileCommand=compileonly,io.github.sooniln.fastcollect.ints.IntHashSet::rehash",
        "-XX:CompileCommand=print,io.github.sooniln.fastcollect.ints.IntHashSet::rehash",
        "-XX:CompileCommand=compileonly,it.unimi.dsi.fastutil.ints.IntOpenHashSet::rehash",
        "-XX:CompileCommand=print,it.unimi.dsi.fastutil.ints.IntOpenHashSet::rehash",
    )
}

registerJitAsm("jitAsmInt2IntHashMapLookup") {
    mainClass = "io.github.sooniln.fastcollect.Int2IntHashMapLookupAsmProbe"
    jvmArgs(
        "-XX:CompileCommand=quiet",
        "-XX:CompileCommand=compileonly,io.github.sooniln.fastcollect.ints.Int2IntHashMap::lookup",
        "-XX:CompileCommand=print,io.github.sooniln.fastcollect.ints.Int2IntHashMap::lookup",
    )
}
