plugins {
    kotlin("multiplatform")
    alias(libs.plugins.kotlinxBenchmark)
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)

    jvm()
    linuxX64()
    mingwX64()
    js {
        nodejs()
    }
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        nodejs()
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.benchmark.runtime)
                implementation(project(":fastcollect"))
                implementation("org.korge:korlibs-datastructure:6.1.0")
            }
        }
        jvmMain {
            dependencies {
                implementation("org.openjdk.jol:jol-core:0.17")
                implementation("it.unimi.dsi:fastutil:8.5.18")
            }
        }
    }
}

benchmark {
    configurations {
        named("main") {}
    }

    targets {
        register("jvm")
        register("linuxX64")
        register("mingwX64")
        register("js")
        register("wasmJs")
    }
}

// try to prevent js benchmarks from running out of memory
tasks.withType<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsExec>().configureEach {
    nodeArgs.add("--max-old-space-size=4096")
}

fun registerMemoryMeasurementTask(name: String, configuration: JavaExec.() -> Unit) = tasks.register<JavaExec>(name) {
    group = "benchmark"
    //dependsOn(tasks.named("compileKotlinJvm"))

    classpath = kotlin.jvm().compilations["main"].runtimeDependencyFiles + kotlin.jvm().compilations["main"].output.allOutputs
    jvmArgs(
        "-Djol.skipHotspotSACheck=true", // suppress warnings about HotSpotSA from appearing in stdout
        "-Djdk.attach.allowAttachSelf=true",
        //"-javaagent:${jvmMainCompilation.runtimeDependencyFiles.first { it.name.startsWith("jol-core") }}"
    )

    configuration()
}

registerMemoryMeasurementTask("runMemoryMeasurement") {
    description = "Measure memory of collections as they grow, output CSV to stdout"
    mainClass = "io.github.sooniln.fastcollect.MemoryMeasurementKt"
}

registerMemoryMeasurementTask("runSplitMemoryMeasurement") {
    description = "Measure memory of splitting fixed data across N collections, output CSV to stdout"
    mainClass = "io.github.sooniln.fastcollect.SplitMemoryMeasurementKt"
}
