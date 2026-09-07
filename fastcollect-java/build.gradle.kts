plugins {
    `java-library`
}

dependencies {
    testImplementation(project(":fastcollect"))
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
