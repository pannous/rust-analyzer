package com.customra.run

import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.ConfigurationFromContext
import com.intellij.execution.actions.RunConfigurationProducer
import com.intellij.openapi.util.Ref

/**
 * Provides run gutter icons (green arrows) for Rustx files.
 * Shows green arrow at the start of the file to run it.
 */
class RustTestLineMarkerProvider : RunLineMarkerContributor() {

    override fun getInfo(element: PsiElement): Info? {
        // Only process Rustx files
        val file = element.containingFile ?: return null
        if (!isRustxFile(file)) return null

        val text = element.text

        // Show marker at the very beginning of the file (first element - usually shebang)
        if (element.parent == file && element.prevSibling == null && text.isNotBlank()) {
            return withExecutorActions(AllIcons.RunConfigurations.TestState.Run)
        }

        // Show marker on test attribute: match "#[" and check next sibling for "test]"
        if (text == "#[") {
            val next = element.nextSibling
            if (next != null && next.text.startsWith("test]")) {
                return withExecutorActions(AllIcons.RunConfigurations.TestState.Run)
            }
        }

        // Show marker on function keyword "fn " when it's a test function
        if (text == "fn " || text == "fn") {
            // Collect the function name from next siblings
            var next = element.nextSibling
            var functionName = ""
            var steps = 0
            while (next != null && steps < 3) {
                val nextText = next.text.trim()
                if (nextText.isNotEmpty() && !nextText.startsWith("{")) {
                    functionName += nextText
                }
                if (nextText.contains("(") || nextText.contains("{")) {
                    break
                }
                next = next.nextSibling
                steps++
            }

            // Check if function name starts with test_
            if (functionName.startsWith("test_")) {
                return withExecutorActions(AllIcons.RunConfigurations.TestState.Run)
            }

            // Check if there's a #[test] attribute before this function
            var prev = element.prevSibling
            var checkSteps = 0
            while (prev != null && checkSteps < 10) {
                val prevText = prev.text.trim()
                if (prevText == "#[" || prevText.startsWith("test]")) {
                    // Found test attribute nearby
                    return withExecutorActions(AllIcons.RunConfigurations.TestState.Run)
                }
                if (prevText.startsWith("fn")) {
                    // Hit another function, stop
                    break
                }
                prev = prev.prevSibling
                checkSteps++
            }
        }

        return null
    }

    private fun isRustxFile(file: PsiFile): Boolean {
        val extension = file.virtualFile?.extension ?: return false
        return extension in RustRunConfigurationProducer.RUST_EXTENSIONS
    }
}
