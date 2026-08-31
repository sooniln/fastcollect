import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import java.net.URLClassLoader
import java.util.ServiceLoader

plugins {
    `java-library`
    alias(libs.plugins.shadow)
    alias(libs.plugins.mavenPublish)
}

val shadowInput = configurations.create("shadowInput")
val abiTools = configurations.create("abiTools")

dependencies {
    shadowInput(project(":fastcollect"))
    abiTools(libs.abi.tools)

    testImplementation(files(tasks.shadowJar))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(17)
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    enabled = false
}

tasks.shadowJar {
    archiveClassifier = ""
    configurations = listOf(shadowInput)

    filesMatching("META-INF/*.kotlin_module") {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
    exclude("META-INF/maven/**")
    exclude("org/jetbrains/annotations/**")

    // relocate kotlin stdlib classes to avoid inteference
    relocate("kotlin", "io.github.sooniln.fastcollect.shaded.kotlin")

    sourceSetsClassesDirs.setFrom(sourceSets.main.map { it.output.classesDirs })

    // TODO: largest cost is currently the Kotlin collection shims (50+ classes pulled in). we could technically get rid
    //  of those by doing JVM actual versions of the collection wrappers which reference java collection classes
    //  directly. we'd need a whole bunch of annoying expect/actual work (and actual doesn't support default
    //  implementations, so even more work...). given that the java jar is currently smaller than the jvm jar anyways,
    //  not worth it at the moment.
    minimize {
        r8 {
            keepRules.add(
                """
                -keep public class !io.github.sooniln.fastcollect.shaded.**, io.github.sooniln.fastcollect.** {
                    !synthetic !bridge public *;
                    !synthetic !bridge protected *;
                }
                -keepattributes Signature,
                                InnerClasses,
                                EnclosingMethod,
                                Deprecated,
                                RuntimeInvisibleAnnotations,
                                RuntimeInvisibleParameterAnnotations,
                                RuntimeInvisibleTypeAnnotations,
                                SourceFile,
                                LineNumberTable,
                                LocalVariableTable,
                                LocalVariableTypeTable
                -dontwarn org.jetbrains.annotations.**
                """.trimIndent()
            )
        }
    }
}

// The bundled dependency lives in `shadowInput`, not `implementation`/`api`, so the "java" component's
// apiElements/runtimeElements report zero dependencies - everything is inside the shadow jar already.
// Their published artifact is redirected below from the (disabled) plain jar to the shadow jar.
listOf("apiElements", "runtimeElements").forEach { name ->
    configurations.named(name) {
        outgoing.artifacts.clear()
        outgoing.artifact(tasks.shadowJar)
    }
}

mavenPublishing {
    publishToMavenCentral()

    val isLocalPublish = gradle.startParameter.taskNames.any { it.endsWith("ToMavenLocal") }
    if (!isLocalPublish) {
        signAllPublications()
    }

    // fastcollect-java has no source of its own (it only repackages fastcollect's compiled output), so the sources
    // and javadoc jars are filled in from fastcollect's JVM target below rather than built from this project.
    configure(JavaLibrary(javadocJar = JavadocJar.None(), sourcesJar = true))

    coordinates(group.toString(), "fastcollect-java", version.toString())

    pom {
        name = "fastcollect-java"
        description = "A library for high-performance primitive collections in the Kotlin ecosystem. This variant has no dependency on the Kotlin standard library."
        licenses {
            license {
                name = "Apache License 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "repo"
                comments = "Covers the bundled, relocated copy of kotlin-stdlib."
            }
        }
    }
}

tasks.named<Jar>("sourcesJar") {
    from(project(":fastcollect").tasks.named<AbstractArchiveTask>("jvmSourcesJar").map { zipTree(it.archiveFile) })
}
publishing.publications.withType<MavenPublication>().configureEach {
    artifact(project(":fastcollect").tasks.named<AbstractArchiveTask>("jvmDokkaJavadocJar"))
}

// The shaded jar is what Java clients actually compile against, so its ABI is checked in - this is what
// catches changes in the set of declarations R8 keeps (including the relocated stdlib classes, which are
// deliberately not filtered out). abi-tools is loaded in an isolated classloader so that it never has to
// go on the buildscript classpath.
@CacheableTask
abstract class DumpJarAbi : DefaultTask() {
    @get:Classpath
    abstract val toolClasspath: ConfigurableFileCollection

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val jar: RegularFileProperty

    @get:OutputFile
    abstract val dumpFile: RegularFileProperty

    @TaskAction
    fun dump() {
        val loader = URLClassLoader(
            toolClasspath.map { it.toURI().toURL() }.toTypedArray(),
            ClassLoader.getPlatformClassLoader(),
        )
        val factoryClass = loader.loadClass("org.jetbrains.kotlin.abi.tools.AbiToolsFactory")
        val toolsClass = loader.loadClass("org.jetbrains.kotlin.abi.tools.AbiTools")
        val filtersClass = loader.loadClass("org.jetbrains.kotlin.abi.tools.AbiFilters")

        val factory = ServiceLoader.load(factoryClass, loader).first()
        val tools = factoryClass.getMethod("get").invoke(factory)
        val noFilters = filtersClass
            .getConstructor(Set::class.java, Set::class.java, Set::class.java, Set::class.java)
            .newInstance(emptySet<String>(), emptySet<String>(), emptySet<String>(), emptySet<String>())

        val abi = StringBuilder()
        toolsClass.getMethod("printJvmDump", Appendable::class.java, Iterable::class.java, filtersClass)
            .invoke(tools, abi, listOf(jar.get().asFile), noFilters)
        dumpFile.get().asFile.writeText(abi.toString())
    }
}

private val abiFile = layout.projectDirectory.file("api/fastcollect-java.api")

val dumpJavaAbi = tasks.register<DumpJarAbi>("dumpJavaAbi") {
    description = "Dumps the ABI of the shaded jar into the build directory."

    toolClasspath.from(abiTools)
    jar = tasks.shadowJar.flatMap { it.archiveFile }
    dumpFile = layout.buildDirectory.file("abi/fastcollect-java.api")
}

tasks.register<Copy>("updateJavaAbi") {
    group = "verification"
    description = "Writes the ABI of the shaded jar to $abiFile."

    from(dumpJavaAbi)
    into(abiFile.asFile.parentFile)
}

val checkJavaAbi = tasks.register("checkJavaAbi") {
    group = "verification"
    description = "Fails if $abiFile is out of date."

    dependsOn(dumpJavaAbi)

    // captured into locals so the action stays configuration-cache-safe
    val actual = dumpJavaAbi.flatMap { it.dumpFile }
    val expectedFile = abiFile.asFile
    doLast {
        val expected = if (expectedFile.exists()) expectedFile.readText() else ""
        check(actual.get().asFile.readText() == expected) {
            "$expectedFile is out of date; run ./gradlew updateJavaAbi to refresh it."
        }
    }
}

tasks.check {
    dependsOn(checkJavaAbi)
}
