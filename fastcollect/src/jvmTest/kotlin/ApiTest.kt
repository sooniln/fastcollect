package io.github.sooniln.fastcollect

import java.io.File
import kotlin.reflect.jvm.kotlinFunction
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Tests the exposed JVM API for mistakes.
 */
class ApiTest {

    private val abiFile = File("api/fastcollect.api")

    private data class AbiFunction(val className: String, val modifiers: List<String>, val name: String)

    private fun parseAbiFunctions(): List<AbiFunction> {
        val functions = mutableListOf<AbiFunction>()
        var currentClass: String? = null

        for (line in abiFile.readLines()) {
            if (line.isEmpty()) continue

            if (!line.startsWith("\t")) {
                currentClass = if (line == "}") null else Regex("""\bclass\s+(\S+)""").find(line)?.groupValues?.get(1)
                continue
            }

            val clazz = currentClass ?: continue
            val tokens = line.trim().split(Regex("\\s+"))
            val funIndex = tokens.indexOf("fun")
            if (funIndex == -1 || funIndex + 1 >= tokens.size) continue

            functions += AbiFunction(clazz, tokens.subList(0, funIndex), tokens[funIndex + 1])
        }

        return functions
    }

    private val valueClassBoilerplateSuffixes = setOf("-impl", "-impl0")

    /**
     * Publicly visible APIs (any API in the API file) should never contain mangled names.
     */
    @Test
    fun unmangledPublicApis() {
        val violations = mutableListOf<String>()

        for ((className, modifiers, name) in parseAbiFunctions()) {
            if ("synthetic" in modifiers) continue
            if (valueClassBoilerplateSuffixes.any { name.endsWith(it) }) continue
            if ('-' !in name) continue

            violations += "$className.$name"
        }

        assertTrue(violations.isEmpty(), violations.joinToString("\n"))
    }

    /**
     * Top level methods are compiled into XXXKt classes by default - this results in non-idiomatic code in Java. This
     * should either be handled by renaming the class wrapping the top level methods (ie to just XXX), or by marking top
     * level methods as synthetic (and thus inaccessible from Java) if their functionality is available to Java in some
     * other fashion.
     */
    @Test
    fun syntheticTopLevelFunctions() {
        val violations = mutableListOf<String>()

        for ((className, modifiers, name) in parseAbiFunctions()) {
            if (!className.endsWith("Kt")) continue
            if ("public" !in modifiers) continue
            if ("synthetic" in modifiers) continue

            violations += "$className.$name"
        }

        assertTrue(violations.isEmpty(), violations.joinToString("\n"))
    }

    val allowedJvmNameMismatches = mapOf<String, String>()

    /**
     * JvmName annotations should match the Kotlin function name wherever possible to reduce API confusion.
     */
    @Test
    fun jvmNameMismatch() {
        val violations = mutableListOf<String>()

        for ((className, modifiers, name) in parseAbiFunctions()) {
            if ("synthetic" in modifiers) continue
            if (valueClassBoilerplateSuffixes.any { name.endsWith(it) }) continue

            val clazz = Class.forName(className.replace('/', '.'), false, javaClass.classLoader)
            val method = clazz.declaredMethods.firstOrNull { it.name == name } ?: continue
            val kotlinName = method.kotlinFunction?.name ?: continue

            if (name != kotlinName) {
                if (allowedJvmNameMismatches[name] == kotlinName) continue

                violations += "Kotlin('$kotlinName') != JVM('$name') in $className"
            }
        }

        assertTrue(violations.isEmpty(), violations.joinToString("\n"))
    }
}
