import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import kotlin.text.lowercase

plugins {
    kotlin("multiplatform")

    alias(libs.plugins.dokka)
    alias(libs.plugins.mavenPublish)
}

repositories {
    mavenCentral()
}

group = "io.github.sooniln"
version = "2.0.2"

private object Generate {
    const val IN_DIR = "src/commonMain/templates"
    const val OUT_DIR = "src/commonGenerated/kotlin"

    object CollectionTypes {
        val Files = listOf(
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

    object ArrayDequeTypes {
        val Files = listOf(
            "ArrayDeque.kte",
        )
        val Expansions = listOf(
            mapOf("Type" to "Byte"),
            mapOf("Type" to "Short"),
            mapOf("Type" to "Int"),
            mapOf("Type" to "Long"),
        )
    }

    object FPArrayDequeTypes {
        val Files = listOf(
            "FPArrayDeque.kte",
        )
        val Expansions = listOf(
            mapOf("Type" to "Float"),
            mapOf("Type" to "Double"),
        )
    }

    object SetTypes {
        val Files = listOf(
            "Set.kte",
        )
        val Expansions = listOf(
            mapOf("Type" to "Int"),
            mapOf("Type" to "Long"),
            mapOf("Type" to "Float"),
            mapOf("Type" to "Double"),
        )
    }

    object HashSetTypes {
        val Files = listOf(
            "HashSet.kte",
        )
        val Expansions = listOf(
            mapOf("Type" to "Int"),
            mapOf("Type" to "Long"),
        )
    }

    object FPHashSetTypes {
        val Files = listOf(
            "FPHashSet.kte",
        )
        val Expansions = listOf(
            mapOf("Type" to "Float"),
            mapOf("Type" to "Double"),
        )
    }

    object MapTypes {
        val Files = listOf(
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

    object HashMapTypes {
        val Files = listOf(
            "HashMap.kte",
        )
        val Expansions = listOf(
            //mapOf("KeyType" to "Int", "ValueType" to "Int", "DefaultValue" to "Int.MIN_VALUE"),
            mapOf("KeyType" to "Int", "ValueType" to "Long", "DefaultValue" to "Long.MIN_VALUE"),
            mapOf("KeyType" to "Int", "ValueType" to "V", "DefaultValue" to "null", "isReferenceValue" to true),
            mapOf("KeyType" to "Long", "ValueType" to "Int", "DefaultValue" to "Int.MIN_VALUE"),
            mapOf("KeyType" to "Long", "ValueType" to "V", "DefaultValue" to "null", "isReferenceValue" to true),
        )
    }

    object StripedHashMapTypes {
        val Files = listOf(
            "StripedHashMap.kte",
        )
        val Expansions = listOf<Map<String, Any>>(
            //mapOf("KVType" to "Int", "DefaultValue" to "Int.MIN_VALUE"),
            mapOf("KVType" to "Long", "DefaultValue" to "Long.MIN_VALUE"),
        )
    }

    object VectorStripedHashMapTypes {
        val Files = listOf(
            "VectorStripedHashMap.kte",
        )
        val Expansions = listOf<Map<String, Any>>(
            mapOf("KVType" to "Int", "ArrayType" to "Long", "DefaultValue" to "Int.MIN_VALUE"),
        )
    }

    object FPHashMapTypes {
        val Files = listOf(
            "FPHashMap.kte",
        )
        val Expansions = listOf(
            mapOf("KeyType" to "Int", "ValueType" to "Float", "DefaultValue" to "Float.NaN"),
            mapOf("KeyType" to "Int", "ValueType" to "Double", "DefaultValue" to "Double.NaN"),
            mapOf("KeyType" to "Long", "ValueType" to "Float", "DefaultValue" to "Float.NaN"),
            mapOf("KeyType" to "Long", "ValueType" to "Double", "DefaultValue" to "Double.NaN"),
        )
    }
}

private fun List<Map<String, Any>>.generateFullExpansion(): List<Map<String, Any>> {
    return map { expansion ->
        buildMap {
            putAll(expansion)

            val type = expansion["Type"] as String?
            if (type != null) {
                putIfAbsent("lowerType", type.lowercase())
                putIfAbsent("subpackage", type.lowercase() + "s")

                val isFPType = type == "Float" || type == "Double"
                putIfAbsent("isFPType", isFPType)
                if (isFPType) {
                    val nonFPType = if (type == "Float") "Int" else "Long"
                    putIfAbsent("NonFPType", nonFPType)
                    putIfAbsent("lowerNonFPType", nonFPType.lowercase())
                }
            }

            val kvType = expansion["KVType"] as String?
            if (kvType != null) {
                putIfAbsent("lowerKVType", kvType.lowercase())
                putIfAbsent("subpackage", kvType.lowercase() + "s")
                putIfAbsent("Name", "${kvType}2${kvType}")
                putIfAbsent("lowerName", "${get("lowerKVType")}2${kvType}")

                val arrayType = expansion["ArrayType"] as String?
                if (arrayType != null) {
                    putIfAbsent("lowerArrayType", arrayType.lowercase())
                }
            }

            val keyType = expansion["KeyType"] as String?
            if (keyType != null) {
                putIfAbsent("lowerKeyType", keyType.lowercase())
                putIfAbsent("keySubpackage", keyType.lowercase() + "s")

                val isFPKey = keyType == "Float" || keyType == "Double"
                putIfAbsent("isFPKey", isFPKey)
                if (isFPKey) {
                    val nonFPKeyType = if (keyType == "Float") "Int" else "Long"
                    putIfAbsent("NonFPKeyType", nonFPKeyType)
                    putIfAbsent("lowerNonFPKeyType", nonFPKeyType.lowercase())
                }
            }

            val valueType = expansion["ValueType"] as String?
            if (valueType != null) {
                putIfAbsent("lowerValueType", valueType.lowercase())
                putIfAbsent("valueSubpackage", valueType.lowercase() + "s")

                val isFPValue = valueType == "Float" || valueType == "Double"
                putIfAbsent("isFPValue", isFPValue)
                if (isFPValue) {
                    val nonFPValueType = if (valueType == "Float") "Int" else "Long"
                    putIfAbsent("NonFPValueType", nonFPValueType)
                    putIfAbsent("lowerNonFPValueType", nonFPValueType.lowercase())
                }
            }

            if (keyType != null && valueType != null) {
                putIfAbsent("subpackage", getValue("keySubpackage"))

                val isReferenceValue = expansion["isReferenceValue"] as Boolean? ?: false
                putIfAbsent("isReferenceValue", isReferenceValue)

                if (isReferenceValue) {
                    putIfAbsent("Name", "${keyType}2Any")
                    putIfAbsent("lowerName", "${get("lowerKeyType")}2Any")
                    putIfAbsent("ValueCollectionType", "Collection")
                    putIfAbsent("ValueIteratorType", "Iterator")
                    putIfAbsent("Nullable", "?")
                    putIfAbsent("Generics", "<$valueType>")
                } else {
                    putIfAbsent("Name", "${keyType}2${valueType}")
                    putIfAbsent("lowerName", "${get("lowerKeyType")}2${valueType}")
                    putIfAbsent("ValueCollectionType", "${valueType}Collection")
                    putIfAbsent("ValueIteratorType", "${valueType}Iterator")
                    putIfAbsent("Nullable", "")
                    putIfAbsent("Generics", "")
                }

                val isFPKeyOrValue = get("isFPKey") as Boolean || get("isFPValue") as Boolean
                putIfAbsent("isFPKeyOrValue", isFPKeyOrValue)
                if (isFPKeyOrValue) {
                    val nonFPKeyType = getOrElse("NonFPKeyType") { getValue("KeyType") } as String
                    val nonFPValueType = getOrElse("NonFPValueType") { getValue("ValueType") } as String

                    put("NonFPName", "${nonFPKeyType}2${nonFPValueType}")
                    putIfAbsent("lowerNonFPName", "${get("lowernonFPKeyType")}2${valueType}")
                }
            }
        }
    }
}

private fun Sync.generate(
        inputFiles: List<String>,
        expansions: List<Map<String, Any>>,
        subpackage: (String, Map<String, Any>) -> String = { _, expansion -> expansion["subpackage"] as String },
        outputFile: (String, Map<String, Any>) -> String) {
    inputFiles.forEach { inputFile ->
        expansions.forEach { expansion ->
            into(subpackage(inputFile, expansion)) {
                from("${Generate.IN_DIR}/$inputFile")
                rename { filename -> outputFile(filename, expansion) }
                expand(*expansion.toList().toTypedArray())
            }
        }
    }
}

tasks.register<Sync>("GenerateCollections") {
    description = "Generates source code for primitively typed collection classes from templates."
    group = "generate"
    into(Generate.OUT_DIR)

    generate(
            Generate.CollectionTypes.Files,
            Generate.CollectionTypes.Expansions.generateFullExpansion()) { filename, expansion ->
        (expansion["Type"] as String) + filename.removeSuffix(".kte") + ".kt"
    }

    generate(
        Generate.ArrayDequeTypes.Files,
        Generate.ArrayDequeTypes.Expansions.generateFullExpansion()) { _, expansion ->
        (expansion["Type"] as String) + "ArrayDeque.kt"
    }

    generate(
        Generate.FPArrayDequeTypes.Files,
        Generate.FPArrayDequeTypes.Expansions.generateFullExpansion()) { _, expansion ->
        (expansion["Type"] as String) + "ArrayDeque.kt"
    }

    generate(
            Generate.SetTypes.Files,
            Generate.SetTypes.Expansions.generateFullExpansion()) { filename, expansion ->
        (expansion["Type"] as String) + filename.removeSuffix(".kte") + ".kt"
    }

    generate(
        Generate.HashSetTypes.Files,
        Generate.HashSetTypes.Expansions.generateFullExpansion()) { _, expansion ->
        (expansion["Type"] as String) + "HashSet.kt"
    }

    generate(
        Generate.FPHashSetTypes.Files,
        Generate.FPHashSetTypes.Expansions.generateFullExpansion()) { _, expansion ->
        (expansion["Type"] as String) + "HashSet.kt"
    }

    generate(
            Generate.MapTypes.Files,
            Generate.MapTypes.Expansions.generateFullExpansion()) { filename, expansion ->
        (expansion["Name"] as String) + filename.removeSuffix(".kte") + ".kt"
    }

    generate(
            Generate.HashMapTypes.Files,
            Generate.HashMapTypes.Expansions.generateFullExpansion()) { _, expansion ->
        (expansion["Name"] as String) + "HashMap.kt"
    }

    generate(
        Generate.StripedHashMapTypes.Files,
        Generate.StripedHashMapTypes.Expansions.generateFullExpansion()) { _, expansion ->
        (expansion["Name"] as String) + "HashMap.kt"
    }

    generate(
        Generate.VectorStripedHashMapTypes.Files,
        Generate.VectorStripedHashMapTypes.Expansions.generateFullExpansion()) { _, expansion ->
        (expansion["Name"] as String) + "HashMap.kt"
    }

    generate(
        Generate.FPHashMapTypes.Files,
        Generate.FPHashMapTypes.Expansions.generateFullExpansion()) { _, expansion ->
        (expansion["Name"] as String) + "HashMap.kt"
    }
}

kotlin {
    jvmToolchain(21)
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
            kotlin.srcDir(tasks.named<Sync>("GenerateCollections"))
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
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
    } else {
        configure(KotlinMultiplatform(javadocJar = JavadocJar.Empty(), sourcesJar = true))
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
