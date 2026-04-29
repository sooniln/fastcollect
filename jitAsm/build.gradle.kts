import org.gradle.kotlin.dsl.java

plugins {
    java
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    implementation(project(":"))
}

tasks.register<JavaExec>("jitAsmIndexOf") {
    group = "verification"
    description = "Print JIT ASM for fastIndexOf vs stdlib indexOf to show the allocation overhead of the FFM wrapping."

    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "io.github.sooniln.fastcollect.IntArrayIndexOfAsmProbe"

    jvmArgs(
        "-Xms256m",
        "-Xmx256m",
        "-Xbatch",
        "-XX:-TieredCompilation",
        "-XX:CICompilerCount=2",
        "-XX:CompileThreshold=1000",
        "-XX:+UnlockDiagnosticVMOptions",
        "-XX:+PrintAssembly",
        "-XX:PrintAssemblyOptions=intel",
        // Only compile and print the two wrapper methods; C2 will inline
        // fastIndexOf / indexOf into them, giving us the full picture.
        "-XX:CompileCommand=quiet",
        "-XX:CompileCommand=compileonly,io.github.sooniln.fastcollect.IntArrayIndexOfAsmProbe::fastSearch",
        "-XX:CompileCommand=compileonly,io.github.sooniln.fastcollect.IntArrayIndexOfAsmProbe::stdSearch",
        "-XX:CompileCommand=print,io.github.sooniln.fastcollect.IntArrayIndexOfAsmProbe::fastSearch",
        "-XX:CompileCommand=print,io.github.sooniln.fastcollect.IntArrayIndexOfAsmProbe::stdSearch",
    )
}

tasks.register<JavaExec>("jitAsm") {
    group = "verification"
    description = "Run a small harness that heats up Int2IntHashMap and prints the generated machine code via -XX:+PrintAssembly (requires hsdis)."

    println("TEST: " + sourceSets["main"].runtimeClasspath.files)

    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "io.github.sooniln.fastcollect.Int2IntMapAsmProbe"

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
        // Reduce noise: try to compile/print only the method we care about.
        "-XX:CompileCommand=quiet",
        "-XX:CompileCommand=compileonly,io.github.sooniln.fastcollect.ints.Int2IntHashMap::lookup",
        "-XX:CompileCommand=print,io.github.sooniln.fastcollect.ints.Int2IntHashMap::lookup",
    )
}
