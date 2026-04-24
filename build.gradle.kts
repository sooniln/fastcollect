@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.dokka)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.mavenPublish)
}

group = "io.github.sooniln"
version = "0.0.1"

object Generate {
    const val IN_DIR = "src/commonMain/templates"
    const val OUT_DIR = "src/commonGenerated/kotlin"

    object AnyTypes {
        val Files = listOf(
            "ArrayDeque.kte",
            "Collection.kte",
            "Iterator.kte",
            "List.kte",
        )
        val Expansions = listOf(
            mapOf("Type" to "Int", "lowerType" to "int", "subpackage" to "ints"),
            mapOf("Type" to "Long", "lowerType" to "long", "subpackage" to "longs"),
            mapOf("Type" to "Float", "lowerType" to "float", "subpackage" to "floats"),
            mapOf("Type" to "Double", "lowerType" to "double", "subpackage" to "doubles"),
        )
    }

    object KeyTypes {
        val Files = listOf(
            "HashSet.kte",
            "Set.kte",
        )
        val Expansions = listOf(
            mapOf("Type" to "Int", "lowerType" to "int", "subpackage" to "ints"),
            mapOf("Type" to "Long", "lowerType" to "long", "subpackage" to "longs"),
        )
    }
}

tasks.register<Copy>("GenerateCollections") {
    group = "generate"
    into(Generate.OUT_DIR)

    Generate.AnyTypes.Files.forEach { filename ->
        Generate.AnyTypes.Expansions.forEach { expansion ->
            into("${expansion["subpackage"]}") {
                from("${Generate.IN_DIR}/$filename")
                rename { filename -> expansion["Type"] + filename.removeSuffix(".kte") + ".kt" }
                expand(*expansion.toList().toTypedArray())
            }
        }
    }

    Generate.KeyTypes.Files.forEach { filename ->
        Generate.KeyTypes.Expansions.forEach { expansion ->
            into("${expansion["subpackage"]}") {
                from("${Generate.IN_DIR}/$filename")
                rename { filename -> expansion["Type"] + filename.removeSuffix(".kte") + ".kt" }
                expand(*expansion.toList().toTypedArray())
            }
        }
    }
}

kotlin {
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
            freeCompilerArgs.add("-Xno-call-assertions")
        }
    }

    // According to https://kotlinlang.org/docs/native-target-support.html
    // Tier 1
    macosArm64()
    iosSimulatorArm64()
    // Tier 2
    linuxX64()
    linuxArm64()
    watchosSimulatorArm64()
    watchosArm32()
    watchosArm64()
    tvosSimulatorArm64()
    tvosArm64()
    iosArm64()
    // Tier 3
    mingwX64()
    iosX64()
    watchosDeviceArm64()

    js {
        browser()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    explicitApi()

    sourceSets {
        commonMain {
            kotlin.srcDir(tasks.named<Copy>("GenerateCollections"))
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

dokka {
    moduleName.set("FastCollect")
    dokkaPublications.html {
        includes.from("README.md")
        suppressInheritedMembers.set(true)
        failOnWarning.set(true)
    }

    dokkaSourceSets.all {
        sourceLink {
            localDirectory.set(file("src/main/kotlin"))
            remoteUrl.set(uri("https://github.com/sooniln/fastcollect/blob/main/"))
            remoteLineSuffix.set("#L")
        }
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates(group.toString(), "fastcollect-kt", version.toString())

    pom {
        name = "fastcollect-kt"
        description = "A library for high-performance primitive collections in the JVM/Kotlin ecosystem."
        inceptionYear = "2026"
        url = "https://github.com/sooniln/fastcollect"
        licenses {
            license {
                name = "MIT License"
                url = "https://github.com/sooniln/fastcollect/blob/main/LICENSE"
                distribution = "repo"
            }
        }
        developers {
            developer {
                id = "sooniln"
                name = "Soonil Nagarkar"
                email = "sooniln@gmail.com"
                organization = "Soonil Nagarkar"
                organizationUrl = "https://github.com/sooniln"
            }
        }
        scm {
            url = "https://github.com/sooniln/fastcollect/"
            connection = "scm:git:git://github.com/sooniln/fastcollect.git"
            developerConnection = "scm:git:ssh://git@github.com/sooniln/fastcollect.git"
        }
    }
}