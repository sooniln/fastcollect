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
                implementation(libs.fastutil)
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
        register("js")
        register("wasmJs")
    }
}
