package com.customra

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class SettingsConfigurer : ProjectActivity {
    private val log = Logger.getInstance(SettingsConfigurer::class.java)

    override suspend fun execute(project: Project) {
        try {
            val binaryPath = BinaryManager.getInstance().getBinaryPath()
            val pathString = binaryPath.toString()

            if (configureViaReflection(pathString)) {
                log.info("Configured rust-analyzer server path: $pathString")
            }
        } catch (e: Exception) {
            log.warn("Failed to configure custom rust-analyzer: ${e.message}", e)
        }
    }

    private fun configureViaReflection(path: String): Boolean {
        return try {
            val settingsClass = Class.forName("org.rust.ide.settings.RsLspExtensionsSettings")
            val getInstance = settingsClass.getMethod("getInstance")
            val settings = getInstance.invoke(null)

            val getter = settingsClass.getMethod("getLocalRustAnalyzerPath")
            val currentPath = getter.invoke(settings) as? String

            if (currentPath != path) {
                val setter = settingsClass.getMethod("setLocalRustAnalyzerPath", String::class.java)
                setter.invoke(settings, path)
                true
            } else {
                false
            }
        } catch (e: ClassNotFoundException) {
            log.info("Rust plugin settings not found, skipping configuration")
            false
        } catch (e: Exception) {
            log.warn("Failed to configure via reflection: ${e.message}")
            false
        }
    }
}
