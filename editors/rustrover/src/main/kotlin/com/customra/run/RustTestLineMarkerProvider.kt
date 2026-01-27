package com.customra.run

import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

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

        // Show marker at the very beginning of the file (first non-whitespace element)
        if (element.parent == file && element.prevSibling == null && text.isNotBlank()) {
            return withExecutorActions(AllIcons.RunConfigurations.TestState.Run)
        }

        // Show marker on test attribute #[test]
        if (text.trim() == "#[test]") {
            return withExecutorActions(AllIcons.RunConfigurations.TestState.Run)
        }

        // Show marker on function keyword when it's a test function
        if (text == "fn") {
            // Check if this is inside a test function by looking ahead
            var next = element.nextSibling
            while (next != null && next.text.trim().isEmpty()) {
                next = next.nextSibling
            }
            // If next element starts with "test_", it's a test function
            if (next != null && next.text.startsWith("test_")) {
                return withExecutorActions(AllIcons.RunConfigurations.TestState.Run)
            }
            // Check if there's a #[test] attribute before this function
            var prev = element.prevSibling
            while (prev != null) {
                val prevText = prev.text.trim()
                if (prevText == "#[test]") {
                    return withExecutorActions(AllIcons.RunConfigurations.TestState.Run)
                }
                if (prevText.isNotEmpty() && !prevText.startsWith("//")) {
                    break
                }
                prev = prev.prevSibling
            }
        }

        return null
    }

    private fun isRustxFile(file: PsiFile): Boolean {
        val extension = file.virtualFile?.extension ?: return false
        return extension in RustRunConfigurationProducer.RUST_EXTENSIONS
    }
}
