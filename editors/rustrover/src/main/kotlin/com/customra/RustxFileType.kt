package com.customra

import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

object RustxFileType : LanguageFileType(RustxLanguage) {
    override fun getName() = "Rustx"
    override fun getDescription() = "Rust with language extensions"
    override fun getDefaultExtension() = "rx"
    override fun getIcon(): Icon? = null
}
