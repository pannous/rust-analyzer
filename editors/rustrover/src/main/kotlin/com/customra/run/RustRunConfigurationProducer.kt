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

        // Check if we're running a specific test function
        val element = sourceElement.get()
        val testName = extractTestName(element)
        if (testName != null) {
            configuration.testFilter = testName
            configuration.name = "Run $testName"
        } else {
            configuration.testFilter = ""
            configuration.name = "Run ${file.nameWithoutExtension}"
        }

        return true
    }

    private fun extractTestName(element: PsiElement?): String? {
        if (element == null) return null

        val text = element.text

        // If this is "fn " token, look ahead for function name
        if (text == "fn " || text == "fn") {
            var next = element.nextSibling
            var functionName = ""
            var steps = 0
            while (next != null && steps < 5) {
                val nextText = next.text.trim()
                if (nextText.isEmpty()) {
                    next = next.nextSibling
                    steps++
                    continue
                }
                if (nextText.contains("(") || nextText.contains("{")) {
                    // Extract just the name part before (
                    val beforeParen = nextText.substringBefore("(").substringBefore("{").trim()
                    if (beforeParen.isNotEmpty()) {
                        functionName += beforeParen
                    }
                    break
                }
                functionName += nextText
                next = next.nextSibling
                steps++
            }

            // Only return if it's a test function
            if (functionName.startsWith("test_") || hasTestAttribute(element)) {
                return functionName.substringBefore("(").substringBefore("{").trim()
            }
        }

        // If this is "#[" token, look ahead for function name
        if (text == "#[") {
            val nextText = element.nextSibling?.text ?: ""
            if (nextText.startsWith("test]")) {
                // Find the fn keyword and extract name
                var next = element.nextSibling
                while (next != null) {
                    if (next.text == "fn " || next.text == "fn") {
                        return extractTestName(next)
                    }
                    next = next.nextSibling
                }
            }
        }

        return null
    }

    private fun hasTestAttribute(fnElement: PsiElement): Boolean {
        var prev = fnElement.prevSibling
        var steps = 0
        while (prev != null && steps < 10) {
            val text = prev.text.trim()
            if (text == "#[" || text.startsWith("test]")) {
                return true
            }
            if (text.startsWith("fn")) {
                break
            }
            prev = prev.prevSibling
            steps++
        }
        return false
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
