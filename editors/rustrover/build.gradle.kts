import java.util.Properties

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.jetbrains.intellij.platform") version "2.2.1"
}

group = "com.customra"

// Auto-increment version from version.properties
val versionProps = Properties().apply {
    file("version.properties").inputStream().use { load(it) }
}
val major = versionProps.getProperty("major")
val minor = versionProps.getProperty("minor")
val patch = versionProps.getProperty("patch").toInt()

val newPatch = patch + 1
versionProps.setProperty("patch", newPatch.toString())
file("version.properties").outputStream().use {
    versionProps.store(it, "Auto-incremented on build")
}

version = "$major.$minor.$newPatch"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        create("RR", providers.gradleProperty("platformVersion").get())
        plugin("com.redhat.devtools.lsp4ij", "0.19.1")
    }
}

kotlin {
    jvmToolchain(21)
}

intellijPlatform {
    pluginConfiguration {
        id = "com.customra.rust-analyzer"
        name = "Custom Rust Analyzer"
        version = project.version.toString()
        description = "Bundles custom rust-analyzer with extended syntax support"
        vendor {
            name = "Custom"
        }
    }
}

tasks {
    buildSearchableOptions {
        enabled = false
    }
}
