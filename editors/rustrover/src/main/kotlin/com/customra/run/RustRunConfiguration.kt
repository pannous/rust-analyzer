package com.customra.run

import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.configurations.RunConfigurationOptions
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import java.io.File

class RustRunConfigurationOptions : RunConfigurationOptions() {
    var scriptPath by string("")
    var arguments by string("")
}

class RustRunConfiguration(
    project: Project,
    factory: ConfigurationFactory,
    name: String
) : RunConfigurationBase<RustRunConfigurationOptions>(project, factory, name) {

    var scriptPath: String
        get() = options.scriptPath ?: ""
        set(value) { options.scriptPath = value }

    var arguments: String
        get() = options.arguments ?: ""
        set(value) { options.arguments = value }

    override fun getOptions(): RustRunConfigurationOptions {
        return super.getOptions() as RustRunConfigurationOptions
    }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        return RustRunConfigurationEditor()
    }

    override fun checkConfiguration() {
        if (scriptPath.isBlank()) {
            throw RuntimeConfigurationError("Rust file path is not specified")
        }
        if (!File(scriptPath).exists()) {
            throw RuntimeConfigurationError("Rust file does not exist: $scriptPath")
        }
    }

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
        return object : CommandLineState(environment) {
            override fun startProcess(): ProcessHandler {
                val file = File(scriptPath)
                val (shellCommand, workDir) = buildCommand(file)

                val commandLine = GeneralCommandLine("sh", "-c", shellCommand)
                    .withWorkDirectory(workDir)

                val handler = ProcessHandlerFactory.getInstance()
                    .createColoredProcessHandler(commandLine)
                ProcessTerminatedListener.attach(handler)
                return handler
            }
        }
    }

    private fun buildCommand(file: File): Pair<String, String> {
        // Check if this is a test file in tests/ directory
        val testInfo = findTestContext(file)
        if (testInfo != null) {
            val (cargoDir, testName) = testInfo
            val cargo = findCustomCargo()
            val runArgs = if (arguments.isNotBlank()) " -- $arguments" else ""
            return Pair("\"$cargo\" test --test $testName$runArgs", cargoDir)
        }

        // Regular file: compile and run with rustc
        val rustc = findCustomRustc()
        val tempDir = System.getProperty("java.io.tmpdir")
        val outputPath = File(tempDir, file.nameWithoutExtension).absolutePath
        val runArgs = if (arguments.isNotBlank()) " $arguments" else ""
        return Pair("\"$rustc\" \"$scriptPath\" -o \"$outputPath\" && \"$outputPath\"$runArgs", file.parent)
    }

    private fun findTestContext(file: File): Pair<String, String>? {
        // Walk up looking for tests/ directory with Cargo.toml in parent
        var current = file.parentFile
        while (current != null) {
            if (current.name == "tests") {
                val cargoDir = current.parentFile
                if (cargoDir != null && File(cargoDir, "Cargo.toml").exists()) {
                    val testName = file.nameWithoutExtension
                    return Pair(cargoDir.absolutePath, testName)
                }
            }
            current = current.parentFile
        }
        return null
    }

    private fun findCustomRustc(): String {
        val customPaths = listOf(
            "/opt/other/rust/build/aarch64-apple-darwin/stage1/bin/rustc",
            "/opt/other/rust/build/host/stage1/bin/rustc",
            System.getProperty("user.home") + "/.rustup/toolchains/custom-rust/bin/rustc"
        )
        return customPaths.firstOrNull { File(it).exists() }
            ?: "rustc" // fallback to PATH
    }

    private fun findCustomCargo(): String {
        val customPaths = listOf(
            "/opt/other/rust/build/aarch64-apple-darwin/stage1/bin/cargo",
            "/opt/other/rust/build/host/stage1/bin/cargo",
            System.getProperty("user.home") + "/.rustup/toolchains/custom-rust/bin/cargo"
        )
        return customPaths.firstOrNull { File(it).exists() }
            ?: "cargo" // fallback to PATH
    }
}
