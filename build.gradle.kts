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
            mapOf("Type" to "Int"),
            mapOf("Type" to "Long"),
            mapOf("Type" to "Float"),
            mapOf("Type" to "Double"),
        )
    }

    object KeyTypes {
        val Files = listOf(
            "HashSet.kte",
            "Set.kte",
        )
        val Expansions = listOf(
            mapOf("Type" to "Int"),
            mapOf("Type" to "Long"),
        )
    }

    object KeyValueTypes {
        val Files = listOf(
            "HashMap.kte",
            "Map.kte",
        )
        val Expansions = listOf(
            mapOf("KeyType" to "Int", "ValueType" to "Int", "DefaultValue" to "Int.MIN_VALUE"),
            mapOf("KeyType" to "Int", "ValueType" to "Long", "DefaultValue" to "Long.MIN_VALUE"),
            mapOf("KeyType" to "Int", "ValueType" to "Float", "DefaultValue" to "Float.NaN"),
            mapOf("KeyType" to "Int", "ValueType" to "Double", "DefaultValue" to "Double.NaN"),
            mapOf("KeyType" to "Long", "ValueType" to "Int", "DefaultValue" to "Int.MIN_VALUE"),
            mapOf("KeyType" to "Long", "ValueType" to "Long", "DefaultValue" to "Long.MIN_VALUE"),
            mapOf("KeyType" to "Long", "ValueType" to "Float", "DefaultValue" to "Float.NaN"),
            mapOf("KeyType" to "Long", "ValueType" to "Double", "DefaultValue" to "Double.NaN"),
        )
    }
}

tasks.register<Copy>("GenerateCollections") {
    description = "Generates source code for primitively typed collection classes from templates."
    group = "generate"
    into(Generate.OUT_DIR)

    val anyTypesExpansions = Generate.AnyTypes.Expansions.map { expansion ->
        buildMap(expansion.size + 2) {
            putAll(expansion)
            put("lowerType", expansion["Type"]!!.lowercase())
            put("subpackage", expansion["Type"]!!.lowercase() + "s")
        }
    }

    Generate.AnyTypes.Files.forEach { filename ->
        anyTypesExpansions.forEach { expansion ->
            into(expansion["subpackage"]!!) {
                from("${Generate.IN_DIR}/$filename")
                rename { filename -> expansion["Type"]!! + filename.removeSuffix(".kte") + ".kt" }
                expand(*expansion.toList().toTypedArray())
            }
        }
    }

    val keyTypesExpansions = Generate.KeyTypes.Expansions.map { expansion ->
        buildMap(expansion.size + 2) {
            putAll(expansion)
            put("lowerType", expansion["Type"]!!.lowercase())
            put("subpackage", expansion["Type"]!!.lowercase() + "s")
        }
    }

    Generate.KeyTypes.Files.forEach { filename ->
        keyTypesExpansions.forEach { expansion ->
            into(expansion["subpackage"]!!) {
                from("${Generate.IN_DIR}/$filename")
                rename { filename -> expansion["Type"]!! + filename.removeSuffix(".kte") + ".kt" }
                expand(*expansion.toList().toTypedArray())
            }
        }
    }

    val keyValueTypesExpansions = Generate.KeyValueTypes.Expansions.map { expansion ->
        buildMap(expansion.size + 2) {
            putAll(expansion)
            put("lowerKeyType", expansion["KeyType"]!!.lowercase())
            put("lowerValueType", expansion["ValueType"]!!.lowercase())
            put("keySubpackage", expansion["KeyType"]!!.lowercase() + "s")
            put("valueSubpackage", expansion["ValueType"]!!.lowercase() + "s")
        }
    }

    Generate.KeyValueTypes.Files.forEach { filename ->
        keyValueTypesExpansions.forEach { expansion ->
            into(expansion["keySubpackage"]!!) {
                from("${Generate.IN_DIR}/$filename")
                rename { filename -> expansion["KeyType"]!! + "2" + expansion["ValueType"]!! + filename.removeSuffix(".kte") + ".kt" }
                expand(*expansion.toList().toTypedArray())
            }
        }
    }
}

kotlin {
    jvmToolchain(17)

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
