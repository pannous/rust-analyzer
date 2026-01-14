plugins {
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.jetbrains.intellij.platform") version "2.2.1"
}

group = "com.customra"
version = providers.gradleProperty("pluginVersion").get()

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        create("RR", providers.gradleProperty("platformVersion").get())
    }
}

kotlin {
    jvmToolchain(21)
}

intellijPlatform {
    pluginConfiguration {
        id = "com.customra.rust-analyzer"
        name = "Custom Rust Analyzer"
        version = providers.gradleProperty("pluginVersion").get()
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
