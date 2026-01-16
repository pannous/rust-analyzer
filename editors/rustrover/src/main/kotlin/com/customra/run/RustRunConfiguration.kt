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
                val rustc = findCustomRustc()
                val file = File(scriptPath)
                val outputPath = File(file.parent, file.nameWithoutExtension).absolutePath
                val workDir = file.parent

                // Combine compile and run into single shell command to avoid blocking EDT
                val runArgs = if (arguments.isNotBlank()) " $arguments" else ""
                val shellCommand = "\"$rustc\" \"$scriptPath\" -o \"$outputPath\" && \"$outputPath\"$runArgs"

                val commandLine = GeneralCommandLine("sh", "-c", shellCommand)
                    .withWorkDirectory(workDir)

                val handler = ProcessHandlerFactory.getInstance()
                    .createColoredProcessHandler(commandLine)
                ProcessTerminatedListener.attach(handler)
                return handler
            }
        }
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
}
