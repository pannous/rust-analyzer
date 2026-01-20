package com.customra.run

import com.intellij.execution.RunManager
import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.ConfigurationFromContext
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement

class RustRunConfigurationProducer : LazyRunConfigurationProducer<RustRunConfiguration>() {

    companion object {
        val RUST_EXTENSIONS = setOf("rs", "rust", "rx", "roo", "🦀", "🐓", "🦘")
    }

    override fun getConfigurationFactory(): ConfigurationFactory {
        return RustRunConfigurationType.getInstance().configurationFactories[0]
    }

    override fun isConfigurationFromContext(
        configuration: RustRunConfiguration,
        context: ConfigurationContext
    ): Boolean {
        val file = context.location?.virtualFile ?: return false
        return file.extension in RUST_EXTENSIONS && configuration.scriptPath == file.path
    }

    override fun setupConfigurationFromContext(
        configuration: RustRunConfiguration,
        context: ConfigurationContext,
        sourceElement: Ref<PsiElement>
    ): Boolean {
        val file = context.location?.virtualFile ?: return false
        if (file.extension !in RUST_EXTENSIONS) return false

        configuration.scriptPath = file.path
        configuration.name = "Run ${file.nameWithoutExtension}"
        return true
    }

    override fun onFirstRun(configuration: ConfigurationFromContext, context: ConfigurationContext, startRunnable: Runnable) {
        val runManager = RunManager.getInstance(context.project)
        val settings = configuration.configurationSettings
        if (!runManager.hasSettings(settings)) {
            runManager.addConfiguration(settings)
        }
        runManager.selectedConfiguration = settings
        startRunnable.run()
    }
}
