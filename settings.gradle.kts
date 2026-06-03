pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "fastcollect"

plugins {
    kotlin("multiplatform") version "2.4.0" apply false
}

include("fastcollect")
include("jmh")
