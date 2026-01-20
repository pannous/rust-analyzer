package com.customra

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange

/**
 * Custom comment toggle action that supports both // and # as comment prefixes.
 * - Commenting adds "// "
 * - Uncommenting removes "// " or "# " (only at line start or within 2 leading spaces)
 */
class RustxCommentAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val project = e.project ?: return
        val document = editor.document
        val file = e.getData(CommonDataKeys.PSI_FILE) ?: return

        if (!isRustxFile(file.name)) return

        WriteCommandAction.runWriteCommandAction(project) {
            val selectionModel = editor.selectionModel
            val startLine: Int
            val endLine: Int

            if (selectionModel.hasSelection()) {
                startLine = document.getLineNumber(selectionModel.selectionStart)
                endLine = document.getLineNumber(selectionModel.selectionEnd)
            } else {
                val line = editor.caretModel.logicalPosition.line
                startLine = line
                endLine = line
            }

            // Check if all non-blank lines are commented
            val allCommented = (startLine..endLine).all { line ->
                val text = getLineText(document, line)
                text.isBlank() || isLineCommented(text)
            }

            // Process lines in reverse to preserve line numbers during edits
            for (line in endLine downTo startLine) {
                val lineText = getLineText(document, line)
                if (lineText.isBlank()) continue

                if (allCommented) {
                    uncommentLine(document, line, lineText)
                } else {
                    commentLine(document, line, lineText)
                }
            }
        }
    }

    override fun update(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.PSI_FILE)
        e.presentation.isEnabledAndVisible = file != null && isRustxFile(file.name)
    }

    private fun isRustxFile(name: String): Boolean {
        val ext = name.substringAfterLast('.', "")
        return ext in setOf("rust", "rx", "roo", "🦀", "🐓", "🦘")
    }

    private fun getLineText(document: Document, line: Int): String {
        val start = document.getLineStartOffset(line)
        val end = document.getLineEndOffset(line)
        return document.getText(TextRange(start, end))
    }

    private fun isLineCommented(lineText: String): Boolean {
        val trimmed = lineText.trimStart()
        if (trimmed.startsWith("//")) return true
        // Check for # at start or within 2 spaces
        val match = Regex("^\\s{0,2}#").find(lineText)
        return match != null
    }

    private fun commentLine(document: Document, line: Int, lineText: String) {
        val lineStart = document.getLineStartOffset(line)
        val indent = lineText.takeWhile { it == ' ' || it == '\t' }
        document.insertString(lineStart + indent.length, "// ")
    }

    private fun uncommentLine(document: Document, line: Int, lineText: String) {
        val lineStart = document.getLineStartOffset(line)

        // Try to match and remove comment prefixes
        // Pattern 1: "// " or "//" at any indentation level
        val slashMatch = Regex("^(\\s*)// ?").find(lineText)
        if (slashMatch != null) {
            val prefixEnd = slashMatch.range.last + 1
            document.deleteString(lineStart + slashMatch.groupValues[1].length, lineStart + prefixEnd)
            return
        }

        // Pattern 2: "# " or "#" only within first 2 spaces
        val hashMatch = Regex("^(\\s{0,2})# ?").find(lineText)
        if (hashMatch != null) {
            val indent = hashMatch.groupValues[1]
            val prefixEnd = hashMatch.range.last + 1
            document.deleteString(lineStart + indent.length, lineStart + prefixEnd)
            return
        }
    }
}
