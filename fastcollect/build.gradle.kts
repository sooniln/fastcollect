import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")

    alias(libs.plugins.dokka)
    alias(libs.plugins.mavenPublish)
}

repositories {
    mavenCentral()
}

group = "io.github.sooniln"
version = "2.0.0"

private object Generate {
    const val IN_DIR = "src/commonMain/templates"
    const val OUT_DIR = "src/commonGenerated/kotlin"

    object CollectionTypes {
        val Files = listOf(
            "ArrayDeque.kte",
            "Collection.kte",
            "Iterator.kte",
            "List.kte",
        )
        val Expansions = listOf(
            mapOf("Type" to "Byte"),
            mapOf("Type" to "Short"),
            mapOf("Type" to "Int"),
            mapOf("Type" to "Long"),
            mapOf("Type" to "Float"),
            mapOf("Type" to "Double"),
        )
    }

    object SetTypes {
        val Files = listOf(
            "HashSet.kte",
            "Set.kte",
        )
        val Expansions = listOf(
            mapOf("Type" to "Int"),
            mapOf("Type" to "Long"),
        )
    }

    object MapTypes {
        val Files = listOf(
            "HashMap.kte",
            "Map.kte",
        )
        val Expansions = listOf(
            mapOf("KeyType" to "Int", "ValueType" to "Int", "DefaultValue" to "Int.MIN_VALUE"),
            mapOf("KeyType" to "Int", "ValueType" to "Long", "DefaultValue" to "Long.MIN_VALUE"),
            mapOf("KeyType" to "Int", "ValueType" to "Float", "DefaultValue" to "Float.NaN"),
            mapOf("KeyType" to "Int", "ValueType" to "Double", "DefaultValue" to "Double.NaN"),
            mapOf("KeyType" to "Int", "ValueType" to "V", "DefaultValue" to "null", "isReferenceValue" to true),
            mapOf("KeyType" to "Long", "ValueType" to "Int", "DefaultValue" to "Int.MIN_VALUE"),
            mapOf("KeyType" to "Long", "ValueType" to "Long", "DefaultValue" to "Long.MIN_VALUE"),
            mapOf("KeyType" to "Long", "ValueType" to "Float", "DefaultValue" to "Float.NaN"),
            mapOf("KeyType" to "Long", "ValueType" to "Double", "DefaultValue" to "Double.NaN"),
            mapOf("KeyType" to "Long", "ValueType" to "V", "DefaultValue" to "null", "isReferenceValue" to true),
        )
    }
}

tasks.register<Copy>("GenerateCollections") {
    description = "Generates source code for primitively typed collection classes from templates."
    group = "generate"
    into(Generate.OUT_DIR)

    val anyTypesExpansions = Generate.CollectionTypes.Expansions.map { expansion ->
        buildMap(expansion.size + 2) {
            putAll(expansion)
            putIfAbsent("lowerType", expansion["Type"]!!.lowercase())
            putIfAbsent("subpackage", expansion["Type"]!!.lowercase() + "s")
        }
    }

    Generate.CollectionTypes.Files.forEach { filename ->
        anyTypesExpansions.forEach { expansion ->
            into(expansion["subpackage"]!!) {
                from("${Generate.IN_DIR}/$filename")
                rename { filename -> expansion["Type"]!! + filename.removeSuffix(".kte") + ".kt" }
                expand(*expansion.toList().toTypedArray())
            }
        }
    }

    val keyTypesExpansions = Generate.SetTypes.Expansions.map { expansion ->
        buildMap(expansion.size + 2) {
            putAll(expansion)
            putIfAbsent("lowerType", expansion["Type"]!!.lowercase())
            putIfAbsent("subpackage", expansion["Type"]!!.lowercase() + "s")
        }
    }

    Generate.SetTypes.Files.forEach { filename ->
        keyTypesExpansions.forEach { expansion ->
            into(expansion["subpackage"]!!) {
                from("${Generate.IN_DIR}/$filename")
                rename { filename -> expansion["Type"]!! + filename.removeSuffix(".kte") + ".kt" }
                expand(*expansion.toList().toTypedArray())
            }
        }
    }

    val keyValueTypesExpansions = Generate.MapTypes.Expansions.map { expansion ->
        buildMap(expansion.size + 2) {
            val isReferenceValue = (expansion["isReferenceValue"] ?: false) as Boolean
            val keyType = expansion["KeyType"] as String
            val lowerKeyType = keyType.lowercase()
            val valueType = expansion["ValueType"] as String
            val lowerValueType = valueType.lowercase()

            putAll(expansion)
            putIfAbsent("lowerKeyType", lowerKeyType)
            putIfAbsent("lowerValueType", lowerValueType)
            putIfAbsent("keySubpackage", lowerKeyType + "s")
            putIfAbsent("valueSubpackage", lowerValueType + "s")
            putIfAbsent("isReferenceValue", isReferenceValue)

            if (isReferenceValue) {
                putIfAbsent("Name", "${keyType}2Any")
                putIfAbsent("lowerName", "${lowerKeyType}2Any")
                putIfAbsent("ValueCollectionType", "Collection")
                putIfAbsent("ValueIteratorType", "Iterator")
                putIfAbsent("Nullable", "?")
                putIfAbsent("Generics", "<$valueType>")
            } else {
                putIfAbsent("Name", "${keyType}2${valueType}")
                putIfAbsent("lowerName", "${lowerKeyType}2${valueType}")
                putIfAbsent("ValueCollectionType", "${valueType}Collection")
                putIfAbsent("ValueIteratorType", "${valueType}Iterator")
                putIfAbsent("Nullable", "")
                putIfAbsent("Generics", "")
            }
        }
    }

    Generate.MapTypes.Files.forEach { filename ->
        keyValueTypesExpansions.forEach { expansion ->
            into(expansion["keySubpackage"]!!) {
                from("${Generate.IN_DIR}/$filename")
                rename { filename -> (expansion["Name"] as String) + filename.removeSuffix(".kte") + ".kt" }
                expand(*expansion.toList().toTypedArray())
            }
        }
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }

    jvmToolchain(17)
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
        testRuns["test"].executionTask.configure {
            useJUnit()
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
        nodejs()
    }
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        nodejs()
    }

    explicitApi()

    applyDefaultHierarchyTemplate()
    sourceSets {
        commonMain {
            kotlin.srcDir(tasks.named<Copy>("GenerateCollections"))
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        jvmTest.dependencies {
            implementation(libs.guava.testlib)
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

    val isLocalPublish = gradle.startParameter.taskNames.any { it.endsWith("ToMavenLocal") }
    if (!isLocalPublish) {
        signAllPublications()
    }

    coordinates(group.toString(), "fastcollect-kotlin", version.toString())

    pom {
        name = "fastcollect"
        description = "A library for high-performance primitive collections in the Kotlin ecosystem."
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
