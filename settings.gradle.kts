pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "fastcollect"

plugins {
    kotlin("multiplatform") version "2.3.20" apply false
}

include("fastcollect")
include("benchmark")
include("jmh")
include("jitAsm")
