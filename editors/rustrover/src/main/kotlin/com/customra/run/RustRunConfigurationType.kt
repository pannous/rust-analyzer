package com.customra.run

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.icons.AllIcons
import javax.swing.Icon

class RustRunConfigurationType : ConfigurationType {
    override fun getDisplayName(): String = "Custom Rust"
    override fun getConfigurationTypeDescription(): String = "Run Rust file with custom compiler"
    override fun getIcon(): Icon = AllIcons.RunConfigurations.Application
    override fun getId(): String = "CustomRustRunConfiguration"

    override fun getConfigurationFactories(): Array<ConfigurationFactory> {
        return arrayOf(RustConfigurationFactory(this))
    }

    companion object {
        fun getInstance(): RustRunConfigurationType {
            return ConfigurationType.CONFIGURATION_TYPE_EP.findExtensionOrFail(RustRunConfigurationType::class.java)
        }
    }
}
