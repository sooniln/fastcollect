import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.abi.BinariesSource
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.dokka)
    alias(libs.plugins.mavenPublish)
}

private val primitiveTypes = listOf("Byte", "Int", "Long", "Float", "Double")
private val setTypes = primitiveTypes - "Byte"
private val keyTypes = listOf("Int", "Long")
private val valueTypes = primitiveTypes + "V"

private val defaultValues = mapOf(
    "Byte" to "Byte.MIN_VALUE",
    "Int" to "Int.MIN_VALUE",
    "Long" to "Long.MIN_VALUE",
    "Float" to "Float.NaN",
    "Double" to "Double.NaN",
    "V" to "null",
)

private fun typeExpansions(types: List<String> = primitiveTypes): List<Map<String, Any>> =
    types.map { mapOf("Type" to it) }

private fun keyValueExpansions(): List<Map<String, Any>> = keyTypes.flatMap { keyType ->
    valueTypes.map { valueType -> mapOf("KeyType" to keyType, "ValueType" to valueType) }
}

tasks.register<Sync>("generateCommonMain") {
    description = "Generates source code for primitively typed collection classes from templates."
    group = "build"
    into("src/commonMainGenerated/kotlin")

    generate(
        "commonMain",
        listOf(
            TemplateInstantiation("ValueTraversable.kte", typeExpansions()) { "${it["Type"]}Traversable.kt" },
            TemplateInstantiation("KeyValueTraversable.kte", keyValueExpansions()) { "${it["Name"]}Traversable.kt" },
            TemplateInstantiation("Iterator.kte", typeExpansions()) { "${it["Type"]}Iterator.kt" },
            TemplateInstantiation("Collection.kte", typeExpansions()) { "${it["Type"]}Collection.kt" },
            TemplateInstantiation("List.kte", typeExpansions()) { "${it["Type"]}List.kt" },
            TemplateInstantiation("ArrayDeque.kte", typeExpansions()) { "${it["Type"]}ArrayDeque.kt" },
            TemplateInstantiation("Set.kte", typeExpansions(setTypes)) { "${it["Type"]}Set.kt" },
            TemplateInstantiation("HashSet.kte", typeExpansions(setTypes)) { "${it["Type"]}HashSet.kt" },
            TemplateInstantiation("Map.kte", keyValueExpansions()) { "${it["Name"]}Map.kt" },
            TemplateInstantiation(
                "HashMap.kte",
                keyValueExpansions().filterNot { it["KeyType"] == "Int" && it["ValueType"] == "Int" },
            ) { "${it["Name"]}HashMap.kt" },
            TemplateInstantiation(
                "InterleavedHashMap.kte",
                listOf(mapOf("KVType" to "Int", "ArrayType" to "Long", "DefaultValue" to "Int.MIN_VALUE")),
            ) { "${it["Name"]}HashMap.kt" },
            TemplateInstantiation("PriorityQueue.kte", typeExpansions()) { "${it["Type"]}PriorityQueue.kt" },
        ),
    )
}

tasks.register<Sync>("generateJvmMain") {
    description = "Generates source code for primitively typed collection classes from templates."
    group = "build"
    into("src/jvmMainGenerated/kotlin")

    generate(
        "jvmMain",
        listOf(
            TemplateInstantiation("JvmValueTraversable.kte", typeExpansions()) { "Jvm${it["Type"]}Traversable.kt" },
            TemplateInstantiation("JvmKeyValueTraversable.kte", keyValueExpansions()) { "Jvm${it["Name"]}Traversable.kt" },
            TemplateInstantiation("JvmMap.kte", keyValueExpansions()) { "Jvm${it["Name"]}Maps.kt" },
            TemplateInstantiation("JvmPriorityQueue.kte", typeExpansions()) { "Jvm${it["Type"]}PriorityQueue.kt" },
        ),
    )
}

kotlin {
    jvmToolchain(17)
    jvm {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_1_8
            jvmDefault = JvmDefaultMode.NO_COMPATIBILITY
        }
        testRuns["test"].executionTask.configure {
            useJUnit()
        }
    }
    macosArm64()
    iosSimulatorArm64()
    iosArm64()
    linuxX64()

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    explicitApi()
    @OptIn(ExperimentalAbiValidation::class)
    abiValidation {
        binariesSource = BinariesSource.MAVEN_PUBLICATIONS
    }

    applyDefaultHierarchyTemplate()
    sourceSets {
        commonMain {
            kotlin.srcDir(tasks.named<Sync>("generateCommonMain"))
        }

        jvmMain {
            kotlin.srcDir(tasks.named<Sync>("generateJvmMain"))
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        jvmTest.dependencies {
            implementation(libs.guava.testlib)
            implementation(kotlin("reflect"))
        }
    }
}

tasks.named("jvmTest") {
    // some tests read the abi file for verifications
    dependsOn("checkKotlinAbi")
}

dokka {
    moduleName = "FastCollect"
    dokkaPublications.html {
        includes.from("README.md")
        suppressInheritedMembers = true
        failOnWarning = true
    }

    dokkaSourceSets.all {
        sourceLink {
            localDirectory = file("src/main/kotlin")
            remoteUrl = uri("https://github.com/sooniln/fastcollect/blob/main/")
            remoteLineSuffix = "#L"
        }
    }
}

mavenPublishing {
    publishToMavenCentral()

    val isLocalPublish = gradle.startParameter.taskNames.any { it.endsWith("ToMavenLocal") }
    if (!isLocalPublish) {
        signAllPublications()
    }

    coordinates(group.toString(), "fastcollect", version.toString())

    // default values come from the POM_* properties in the root gradle.properties
    pom {
        name = "fastcollect"
        description = "A library for high-performance primitive collections in the Kotlin ecosystem."
    }
}

private data class TemplateInstantiation(
    val inputFile: String,
    val expansions: List<Map<String, Any>>,
    val outputFile: (Map<String, Any>) -> String,
)

private fun Map<String, Any>.generateFullExpansion(): Map<String, Any> {
    val map = this
    return buildMap {
        putAll(map)

        val type = map["Type"] as String?
        if (type != null) {
            putIfAbsent("lowerType", type.lowercase())
        }

        val kvType = map["KVType"] as String?
        if (kvType != null) {
            putIfAbsent("lowerKVType", kvType.lowercase())
            putIfAbsent("Name", "${kvType}2${kvType}")
            putIfAbsent("lowerName", "${get("lowerKVType")}2${kvType}")

            val arrayType = map["ArrayType"] as String?
            if (arrayType != null) {
                putIfAbsent("lowerArrayType", arrayType.lowercase())
            }
        }

        val keyType = map["KeyType"] as String?
        if (keyType != null) {
            putIfAbsent("lowerKeyType", keyType.lowercase())
        }

        val valueType = map["ValueType"] as String?
        if (valueType != null) {
            putIfAbsent("lowerValueType", valueType.lowercase())
        }

        if (keyType != null && valueType != null) {
            val isReferenceValue = valueType == "V"
            putIfAbsent("isReferenceValue", isReferenceValue)
            putIfAbsent("DefaultValue", defaultValues.getValue(valueType))

            if (isReferenceValue) {
                putIfAbsent("Name", "${keyType}2Any")
                putIfAbsent("lowerName", "${get("lowerKeyType")}2Any")
                putIfAbsent("ValueCollectionType", "Collection")
                putIfAbsent("ValueIteratorType", "Iterator")
                putIfAbsent("Nullable", "?")
                putIfAbsent("Generics", "<$valueType>")
                putIfAbsent("OutGenerics", "<out $valueType>")
                putIfAbsent("StarGenerics", "<*>")
            } else {
                putIfAbsent("Name", "${keyType}2${valueType}")
                putIfAbsent("lowerName", "${get("lowerKeyType")}2${valueType}")
                putIfAbsent("ValueCollectionType", "${valueType}Collection")
                putIfAbsent("ValueIteratorType", "${valueType}Iterator")
                putIfAbsent("Nullable", "")
                putIfAbsent("Generics", "")
                putIfAbsent("OutGenerics", "")
                putIfAbsent("StarGenerics", "")
            }
        }
    }
}

private fun Sync.generate(sourceSet: String, templates: List<TemplateInstantiation>) {
    templates.forEach { template ->
        template.expansions.forEach { expansion ->
            val fullExpansion = expansion.generateFullExpansion()
            val outputFile = template.outputFile(fullExpansion)
            check(outputFile.endsWith(".kt")) { "$outputFile must end with .kt" }
            from("src/$sourceSet/templates/${template.inputFile}") {
                rename { outputFile }
                expand(fullExpansion)
            }
        }
    }
}
