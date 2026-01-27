package com.customra.run

import com.intellij.execution.lineMarker.ExecutorAction
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.LeafPsiElement

/**
 * Provides run gutter icons (green arrows) for Rust test functions and main file markers.
 * Shows icons for:
 * - Functions with #[test] attribute
 * - Functions starting with "test_"
 * - Top of file (for running the entire file)
 */
class RustTestLineMarkerProvider : RunLineMarkerContributor() {

    override fun getInfo(element: PsiElement): Info? {
        // Only process Rustx files
        val file = element.containingFile ?: return null
        if (!isRustxFile(file)) return null

        // Check if this is the first element (for file-level run marker)
        if (isFirstElement(element)) {
            return withExecutorActions(AllIcons.RunConfigurations.TestState.Run)
        }

        // Check for test functions
        val text = element.text
        if (element is LeafPsiElement && text == "fn") {
            if (isTestFunction(element)) {
                return withExecutorActions(AllIcons.RunConfigurations.TestState.Run)
            }
        }

        return null
    }

    private fun isFirstElement(element: PsiElement): Boolean {
        val file = element.containingFile
        var current: PsiElement? = file.firstChild
        while (current != null) {
            if (current == element) return true
            if (current.text.isNotBlank() && current.text.trim().isNotEmpty()) {
                return false
            }
            current = current.nextSibling
        }
        return false
    }

    private fun isTestFunction(fnElement: PsiElement): Boolean {
        // Look backwards for #[test] attribute
        var prev: PsiElement? = fnElement.prevSibling
        var checkCount = 0
        while (prev != null && checkCount < 10) {
            val text = prev.text.trim()
            if (text.contains("#[test]") || text.contains("# [test]")) {
                return true
            }
            // Stop at previous function or other major construct
            if (text.contains("fn ") && prev != fnElement) {
                break
            }
            prev = prev.prevSibling
            checkCount++
        }

        // Look forward for function name starting with "test_"
        var next: PsiElement? = fnElement.nextSibling
        var forwardCheckCount = 0
        while (next != null && forwardCheckCount < 5) {
            val text = next.text.trim()
            if (text.matches(Regex("^test_\\w+"))) {
                return true
            }
            // Stop at opening brace
            if (text.contains("{")) {
                break
            }
            next = next.nextSibling
            forwardCheckCount++
        }

        return false
    }

    private fun isRustxFile(file: PsiFile): Boolean {
        val extension = file.virtualFile?.extension ?: return false
        return extension in RustRunConfigurationProducer.RUST_EXTENSIONS
    }
}
