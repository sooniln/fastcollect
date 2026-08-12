import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import org.gradle.kotlin.dsl.kotlin
import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.abi.BinariesSource
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation
import kotlin.text.lowercase
import java.nio.file.Path as NioPath

plugins {
    kotlin("multiplatform")

    alias(libs.plugins.dokka)
    alias(libs.plugins.mavenPublish)
}

repositories {
    mavenCentral()
}

group = "io.github.sooniln"
version = "4.0.1"

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

            val isFPType = type == "Float" || type == "Double"
            putIfAbsent("isFPType", isFPType)
            if (isFPType) {
                val nonFPType = if (type == "Float") "Int" else "Long"
                putIfAbsent("NonFPType", nonFPType)
                putIfAbsent("lowerNonFPType", nonFPType.lowercase())
            }
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

            val isFPKey = keyType == "Float" || keyType == "Double"
            putIfAbsent("isFPKey", isFPKey)
            if (isFPKey) {
                val nonFPKeyType = if (keyType == "Float") "Int" else "Long"
                putIfAbsent("NonFPKeyType", nonFPKeyType)
                putIfAbsent("lowerNonFPKeyType", nonFPKeyType.lowercase())
            }
        }

        val valueType = map["ValueType"] as String?
        if (valueType != null) {
            putIfAbsent("lowerValueType", valueType.lowercase())

            val isFPValue = valueType == "Float" || valueType == "Double"
            putIfAbsent("isFPValue", isFPValue)
            if (isFPValue) {
                val nonFPValueType = if (valueType == "Float") "Int" else "Long"
                putIfAbsent("NonFPValueType", nonFPValueType)
                putIfAbsent("lowerNonFPValueType", nonFPValueType.lowercase())
            }
        }

        if (keyType != null && valueType != null) {
            val isReferenceValue = map["isReferenceValue"] as Boolean? ?: false
            putIfAbsent("isReferenceValue", isReferenceValue)

            if (isReferenceValue) {
                putIfAbsent("Name", "${keyType}2Any")
                putIfAbsent("lowerName", "${get("lowerKeyType")}2Any")
                putIfAbsent("ValueCollectionType", "Collection")
                putIfAbsent("ValueIteratorType", "Iterator")
                putIfAbsent("Nullable", "?")
                putIfAbsent("Generics", "<$valueType>")
                putIfAbsent("StarGenerics", "<*>")
            } else {
                putIfAbsent("Name", "${keyType}2${valueType}")
                putIfAbsent("lowerName", "${get("lowerKeyType")}2${valueType}")
                putIfAbsent("ValueCollectionType", "${valueType}Collection")
                putIfAbsent("ValueIteratorType", "${valueType}Iterator")
                putIfAbsent("Nullable", "")
                putIfAbsent("Generics", "")
                putIfAbsent("StarGenerics", "")
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

private fun Sync.generate(sourceSet: String, templates: List<TemplateInstantiation>) {
    templates.forEach { template ->
        template.expansions.forEach { expansion ->
            val fullExpansion = expansion.generateFullExpansion()
            val path = NioPath.of(template.outputFile(fullExpansion))
            val outputFolder = (path.parent ?: NioPath.of(".")).toString()
            val outputFile = path.fileName.toString()
            check(outputFile.endsWith(".kt")) { "$outputFile must end with .kt" }
            into(outputFolder) {
                from("src/$sourceSet/templates/${template.inputFile}")
                rename { outputFile }
                expand(*fullExpansion.toList().toTypedArray())
            }
        }
    }
}

tasks.register<Sync>("GenerateCommonMain") {
    description = "Generates source code for primitively typed collection classes from templates."
    group = "build"
    into("src/commonMainGenerated/kotlin")

    generate("commonMain",
        listOf(
            TemplateInstantiation(
                "Predicate.kte",
                listOf(
                    mapOf("Type" to "Byte"),
                    mapOf("Type" to "Int"),
                    mapOf("Type" to "Long"),
                    mapOf("Type" to "Float"),
                    mapOf("Type" to "Double"),
                )) { expansion -> "${expansion["Type"]}Predicate.kt" },
            TemplateInstantiation(
                "Iterator.kte",
                listOf(
                    mapOf("Type" to "Byte"),
                    mapOf("Type" to "Int"),
                    mapOf("Type" to "Long"),
                    mapOf("Type" to "Float"),
                    mapOf("Type" to "Double"),
                )) { expansion -> "${expansion["Type"]}Iterator.kt" },
            TemplateInstantiation(
                "Collection.kte",
                listOf(
                    mapOf("Type" to "Byte"),
                    mapOf("Type" to "Int"),
                    mapOf("Type" to "Long"),
                    mapOf("Type" to "Float"),
                    mapOf("Type" to "Double"),
                )) { expansion -> "${expansion["Type"]}Collection.kt" },
            TemplateInstantiation(
                "List.kte",
                listOf(
                    mapOf("Type" to "Byte"),
                    mapOf("Type" to "Int"),
                    mapOf("Type" to "Long"),
                    mapOf("Type" to "Float"),
                    mapOf("Type" to "Double"),
                )) { expansion -> "${expansion["Type"]}List.kt" },
            TemplateInstantiation(
                "ArrayDeque.kte",
                listOf(
                    mapOf("Type" to "Byte"),
                    mapOf("Type" to "Int"),
                    mapOf("Type" to "Long"),
                    mapOf("Type" to "Float"),
                    mapOf("Type" to "Double"),
                )
            ) { expansion -> "${expansion["Type"]}ArrayDeque.kt" },
            TemplateInstantiation(
                "Set.kte",
                listOf(
                    mapOf("Type" to "Int"),
                    mapOf("Type" to "Long"),
                    mapOf("Type" to "Float"),
                    mapOf("Type" to "Double"),
                )) { expansion -> "${expansion["Type"]}Set.kt" },
            TemplateInstantiation(
                "HashSet.kte",
                listOf(
                    mapOf("Type" to "Int"),
                    mapOf("Type" to "Long"),
                    mapOf("Type" to "Float"),
                    mapOf("Type" to "Double"),
                )) { expansion -> "${expansion["Type"]}HashSet.kt" },
            TemplateInstantiation(
                "Map.kte",
                listOf(
                    mapOf("KeyType" to "Int", "ValueType" to "Byte", "DefaultValue" to "Byte.MIN_VALUE"),
                    mapOf("KeyType" to "Int", "ValueType" to "Int", "DefaultValue" to "Int.MIN_VALUE"),
                    mapOf("KeyType" to "Int", "ValueType" to "Long", "DefaultValue" to "Long.MIN_VALUE"),
                    mapOf("KeyType" to "Int", "ValueType" to "Float", "DefaultValue" to "Float.NaN"),
                    mapOf("KeyType" to "Int", "ValueType" to "Double", "DefaultValue" to "Double.NaN"),
                    mapOf("KeyType" to "Int", "ValueType" to "V", "DefaultValue" to "null", "isReferenceValue" to true),
                    mapOf("KeyType" to "Long", "ValueType" to "Byte", "DefaultValue" to "Byte.MIN_VALUE"),
                    mapOf("KeyType" to "Long", "ValueType" to "Int", "DefaultValue" to "Int.MIN_VALUE"),
                    mapOf("KeyType" to "Long", "ValueType" to "Long", "DefaultValue" to "Long.MIN_VALUE"),
                    mapOf("KeyType" to "Long", "ValueType" to "Float", "DefaultValue" to "Float.NaN"),
                    mapOf("KeyType" to "Long", "ValueType" to "Double", "DefaultValue" to "Double.NaN"),
                    mapOf("KeyType" to "Long", "ValueType" to "V", "DefaultValue" to "null", "isReferenceValue" to true),
                )) { expansion -> "${expansion["Name"]}Map.kt" },
            TemplateInstantiation(
                "HashMap.kte",
                listOf(
                    mapOf("KeyType" to "Int", "ValueType" to "Byte", "DefaultValue" to "Byte.MIN_VALUE"),
                    mapOf("KeyType" to "Int", "ValueType" to "Long", "DefaultValue" to "Long.MIN_VALUE"),
                    mapOf("KeyType" to "Int", "ValueType" to "V", "DefaultValue" to "null", "isReferenceValue" to true),
                    mapOf("KeyType" to "Long", "ValueType" to "Byte", "DefaultValue" to "Byte.MIN_VALUE"),
                    mapOf("KeyType" to "Long", "ValueType" to "Int", "DefaultValue" to "Int.MIN_VALUE"),
                    mapOf("KeyType" to "Long", "ValueType" to "Long", "DefaultValue" to "Long.MIN_VALUE"),
                    mapOf("KeyType" to "Long", "ValueType" to "V", "DefaultValue" to "null", "isReferenceValue" to true),
                )) { expansion -> "${expansion["Name"]}HashMap.kt" },
            TemplateInstantiation(
                "InterleavedHashMap.kte",
                listOf(
                    mapOf("KVType" to "Int", "ArrayType" to "Long", "DefaultValue" to "Int.MIN_VALUE"),
                )) { expansion -> "${expansion["Name"]}HashMap.kt" },
            TemplateInstantiation(
                "FPHashMap.kte",
                listOf(
                    mapOf("KeyType" to "Int", "ValueType" to "Float", "DefaultValue" to "Float.NaN"),
                    mapOf("KeyType" to "Int", "ValueType" to "Double", "DefaultValue" to "Double.NaN"),
                    mapOf("KeyType" to "Long", "ValueType" to "Float", "DefaultValue" to "Float.NaN"),
                    mapOf("KeyType" to "Long", "ValueType" to "Double", "DefaultValue" to "Double.NaN"),
                )) { expansion -> "${expansion["Name"]}HashMap.kt" },
            TemplateInstantiation(
                "PriorityQueue.kte",
                listOf(
                    mapOf("Type" to "Byte"),
                    mapOf("Type" to "Int"),
                    mapOf("Type" to "Long"),
                    mapOf("Type" to "Float"),
                    mapOf("Type" to "Double"),
                )) { expansion -> "${expansion["Type"]}PriorityQueue.kt" },
        ))
}

tasks.register<Sync>("GenerateJvmMain") {
    description = "Generates source code for primitively typed collection classes from templates."
    group = "build"
    into("src/jvmMainGenerated/kotlin")

    generate("jvmMain",
        listOf(
            TemplateInstantiation(
                "JvmPriorityQueue.kte",
                listOf(
                    mapOf("Type" to "Byte"),
                    mapOf("Type" to "Int"),
                    mapOf("Type" to "Long"),
                    mapOf("Type" to "Float"),
                    mapOf("Type" to "Double"),
                )
            ) { expansion -> "Jvm${expansion["Type"]}PriorityQueue.kt" },
        ))
}

kotlin {
    jvmToolchain(21)
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
            jvmDefault.set(JvmDefaultMode.NO_COMPATIBILITY)
        }
        testRuns["test"].executionTask.configure {
            useJUnit()
        }
    }

    // According to https://kotlinlang.org/docs/native-target-support.html
    // Tier 1
    macosArm64()
    // Tier 2
    linuxX64()
    linuxArm64()
    iosArm64()
    // Tier 3
    mingwX64()
    iosX64()

    explicitApi()
    @OptIn(ExperimentalAbiValidation::class)
    abiValidation {
        binariesSource.set(BinariesSource.MAVEN_PUBLICATIONS)
    }

    applyDefaultHierarchyTemplate()
    sourceSets {
        commonMain {
            kotlin.srcDir(tasks.named<Sync>("GenerateCommonMain"))
        }

        jvmMain {
            kotlin.srcDir(tasks.named<Sync>("GenerateJvmMain"))
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
