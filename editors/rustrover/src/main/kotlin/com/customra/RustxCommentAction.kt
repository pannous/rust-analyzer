package com.customra

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor

/**
 * Custom comment toggle action that supports both // and # as comment prefixes.
 * Commenting adds //, uncommenting removes // or # (at line start or after up to 2 spaces).
 */
class RustxCommentAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val project = e.project ?: return
        val document = editor.document
        val file = e.getData(CommonDataKeys.PSI_FILE) ?: return

        // Only handle our file types
        if (!isRustxFile(file.name)) return

        WriteCommandAction.runWriteCommandAction(project) {
            val caret = editor.caretModel.primaryCaret
            val selectionModel = editor.selectionModel

            if (selectionModel.hasSelection()) {
                toggleCommentForSelection(document, selectionModel.selectionStart, selectionModel.selectionEnd)
            } else {
                toggleCommentForLine(document, caret.logicalPosition.line)
            }
        }
    }

    override fun update(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.PSI_FILE)
        e.presentation.isEnabledAndVisible = file != null && isRustxFile(file.name)
    }

    private fun isRustxFile(name: String): Boolean {
        val ext = name.substringAfterLast('.', "")
        return ext in setOf("rust", "rx", "roo", "🦀", "🐓", "🦘") ||
               name.endsWith(".rs") // include .rs for consistency
    }

    private fun toggleCommentForSelection(document: Document, start: Int, end: Int) {
        val startLine = document.getLineNumber(start)
        val endLine = document.getLineNumber(end)

        // Check if all lines are commented
        val allCommented = (startLine..endLine).all { isLineCommented(document, it) }

        for (line in startLine..endLine) {
            if (allCommented) {
                uncommentLine(document, line)
            } else {
                commentLine(document, line)
            }
        }
    }

    private fun toggleCommentForLine(document: Document, line: Int) {
        if (isLineCommented(document, line)) {
            uncommentLine(document, line)
        } else {
            commentLine(document, line)
        }
    }

    private fun isLineCommented(document: Document, line: Int): Boolean {
        val lineStart = document.getLineStartOffset(line)
        val lineEnd = document.getLineEndOffset(line)
        val lineText = document.getText(com.intellij.openapi.util.TextRange(lineStart, lineEnd))
        val trimmed = lineText.trimStart()
        return trimmed.startsWith("//") || trimmed.startsWith("#")
    }

    private fun commentLine(document: Document, line: Int) {
        val lineStart = document.getLineStartOffset(line)
        val lineEnd = document.getLineEndOffset(line)
        val lineText = document.getText(com.intellij.openapi.util.TextRange(lineStart, lineEnd))

        if (lineText.isBlank()) return

        // Find the indentation
        val indent = lineText.takeWhile { it == ' ' || it == '\t' }
        document.insertString(lineStart + indent.length, "// ")
    }

    private fun uncommentLine(document: Document, line: Int) {
        val lineStart = document.getLineStartOffset(line)
        val lineEnd = document.getLineEndOffset(line)
        val lineText = document.getText(com.intellij.openapi.util.TextRange(lineStart, lineEnd))

        // Match patterns: "// ", "//", "# ", "#" at start or after whitespace (up to 2 spaces)
        val patterns = listOf(
            Regex("^(\\s{0,2})// "),  // "// " with optional leading space (0-2)
            Regex("^(\\s{0,2})//"),    // "//" with optional leading space (0-2)
            Regex("^(\\s{0,2})# "),    // "# " with optional leading space (0-2)
            Regex("^(\\s{0,2})#"),     // "#" with optional leading space (0-2)
        )

        for (pattern in patterns) {
            val match = pattern.find(lineText)
            if (match != null) {
                val leadingWhitespace = match.groupValues[1]
                val toRemove = match.value.length - leadingWhitespace.length
                document.deleteString(lineStart + leadingWhitespace.length, lineStart + leadingWhitespace.length + toRemove)
                return
            }
        }
    }
}
