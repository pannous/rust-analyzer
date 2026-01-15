package com.customra.run

import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.RunManager
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys

class RustRunAction : AnAction("Run with Custom Rust") {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        if (file.extension !in RustRunConfigurationProducer.RUST_EXTENSIONS) return

        val runManager = RunManager.getInstance(project)
        val factory = RustRunConfigurationType.getInstance().configurationFactories[0]
        val settings = runManager.createConfiguration("Run ${file.nameWithoutExtension}", factory)
        val configuration = settings.configuration as RustRunConfiguration

        configuration.scriptPath = file.path
        runManager.addConfiguration(settings)
        runManager.selectedConfiguration = settings

        ProgramRunnerUtil.executeConfiguration(settings, DefaultRunExecutor.getRunExecutorInstance())
    }

    override fun update(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        e.presentation.isEnabledAndVisible = file?.extension in RustRunConfigurationProducer.RUST_EXTENSIONS
    }
}
