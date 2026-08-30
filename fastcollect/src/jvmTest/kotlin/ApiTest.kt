package io.github.sooniln.fastcollect

import java.io.File
import java.lang.reflect.Method
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.javaGetter
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.javaSetter
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

    /**
     * A JvmName rename of a virtual method/property must be propagated to every publicly visible override, otherwise
     * Java clients see the un-renamed name of the override rather than the name declared by the supertype.
     */
    @Test
    fun overrideNameMismatch() {
        val violations = mutableListOf<String>()
        val abiFunctions = parseAbiFunctions()
        val publicApi = abiFunctions.mapTo(mutableSetOf()) { it.className to it.name }

        for (className in abiFunctions.map { it.className }.distinct()) {
            val clazz = Class.forName(className.replace('/', '.'), false, javaClass.classLoader)
            val supertypes = clazz.allSupertypes()

            for ((kotlinName, method) in clazz.declaredKotlinMembers()) {
                if (className to method.name !in publicApi) continue
                if (method.isSynthetic || method.isBridge) continue
                if (valueClassBoilerplateSuffixes.any { method.name.endsWith(it) }) continue

                for (supertype in supertypes) {
                    val parent = supertype.declaredKotlinMembers().firstOrNull {
                        it.kotlinName == kotlinName &&
                            !it.method.isSynthetic &&
                            !it.method.isBridge &&
                            it.method.parameterTypes.contentEquals(method.parameterTypes)
                    } ?: continue

                    if (parent.method.name != method.name) {
                        violations += "$className.${method.name} overrides ${supertype.name}.${parent.method.name} " +
                            "(Kotlin '$kotlinName')"
                    }
                }
            }
        }

        assertTrue(violations.isEmpty(), violations.joinToString("\n"))
    }

    private fun Class<*>.allSupertypes(): Set<Class<*>> {
        val supertypes = linkedSetOf<Class<*>>()
        val pending = ArrayDeque(listOfNotNull(superclass) + interfaces)

        while (pending.isNotEmpty()) {
            val supertype = pending.removeFirst()
            if (!supertypes.add(supertype)) continue

            pending += listOfNotNull(supertype.superclass) + supertype.interfaces
        }

        return supertypes
    }

    private data class JvmMember(val kotlinName: String, val method: Method)

    private val declaredKotlinMembers = mutableMapOf<Class<*>, List<JvmMember>>()

    private fun Class<*>.declaredKotlinMembers(): List<JvmMember> = declaredKotlinMembers.getOrPut(this) {
        if (getAnnotation(Metadata::class.java)?.kind != 1) return@getOrPut emptyList()

        kotlin.declaredMemberFunctions.mapNotNull { function ->
            function.javaMethod?.let { JvmMember(function.name, it) }
        } + kotlin.declaredMemberProperties.flatMap { property ->
            listOfNotNull(
                property.javaGetter?.let { JvmMember(property.name, it) },
                (property as? KMutableProperty1<*, *>)?.javaSetter?.let { JvmMember(property.name, it) },
            )
        }
    }
}
