package com.customra.run

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel

class RustRunConfigurationEditor : SettingsEditor<RustRunConfiguration>() {
    private val scriptPathField = TextFieldWithBrowseButton()
    private val argumentsField = JBTextField()

    init {
        scriptPathField.addBrowseFolderListener(
            "Select Rust File",
            "Select the Rust file to run",
            null,
            FileChooserDescriptorFactory.createSingleFileDescriptor("rs")
        )
    }

    override fun createEditor(): JComponent {
        return FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("Rust file:"), scriptPathField, 1, false)
            .addLabeledComponent(JBLabel("Arguments:"), argumentsField, 1, false)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    override fun resetEditorFrom(configuration: RustRunConfiguration) {
        scriptPathField.text = configuration.scriptPath
        argumentsField.text = configuration.arguments
    }

    override fun applyEditorTo(configuration: RustRunConfiguration) {
        configuration.scriptPath = scriptPathField.text
        configuration.arguments = argumentsField.text
    }
}
