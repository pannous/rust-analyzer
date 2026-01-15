package com.customra

import com.intellij.openapi.project.Project
import com.redhat.devtools.lsp4ij.LanguageServerFactory
import com.redhat.devtools.lsp4ij.server.ProcessStreamConnectionProvider
import com.redhat.devtools.lsp4ij.server.StreamConnectionProvider

class CustomRustAnalyzerServerFactory : LanguageServerFactory {

    override fun createConnectionProvider(project: Project): StreamConnectionProvider {
        val binaryPath = BinaryManager.getInstance().getBinaryPath().toString()
        return RustAnalyzerConnectionProvider(binaryPath, project.basePath)
    }
}

class RustAnalyzerConnectionProvider(
    binaryPath: String,
    workingDir: String?
) : ProcessStreamConnectionProvider() {
    init {
        commands = listOf(binaryPath)
        workingDir?.let { workingDirectory = it }
    }
}
