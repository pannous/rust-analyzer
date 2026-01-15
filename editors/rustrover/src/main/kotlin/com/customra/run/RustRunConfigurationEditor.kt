package com.customra.run

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel

class RustRunConfigurationEditor : SettingsEditor<RustRunConfiguration>() {
    private val scriptPathField = TextFieldWithBrowseButton()
    private val argumentsField = JBTextField()

    init {
        val descriptor = object : FileChooserDescriptor(true, false, false, false, false, false) {
            override fun isFileSelectable(file: VirtualFile?): Boolean {
                return file?.extension in RustRunConfigurationProducer.RUST_EXTENSIONS
            }
        }.withTitle("Select Rust File")
         .withDescription("Select the Rust file to run (.rs, .rx, .roo, .🦀, .🐓, .🦘)")

        scriptPathField.addBrowseFolderListener(null, descriptor)
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
