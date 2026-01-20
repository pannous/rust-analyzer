package com.customra

import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

object RustxFileType : LanguageFileType(RustxLanguage) {
    private val ICON = IconLoader.getIcon("/icons/rust.svg", RustxFileType::class.java)

    override fun getName() = "Rustx"
    override fun getDescription() = "Rust with language extensions"
    override fun getDefaultExtension() = "rx"
    override fun getIcon(): Icon = ICON
}
