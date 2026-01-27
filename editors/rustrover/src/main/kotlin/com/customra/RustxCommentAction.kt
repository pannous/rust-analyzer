package com.customra

import com.intellij.codeInsight.generation.actions.CommentByLineCommentAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.util.TextRange

/**
 * Overrides the built-in CommentByLineCommentAction to support both // and # prefixes
 * for Rustx files. For other files, delegates to parent implementation.
 */
class RustxCommentAction : CommentByLineCommentAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        val project = e.project
        val file = e.getData(CommonDataKeys.PSI_FILE)

        // Delegate to parent for non-Rustx files
        if (editor == null || project == null || file == null || !isRustxFile(file.name)) {
            super.actionPerformed(e)
            return
        }

        val document = editor.document

        WriteCommandAction.runWriteCommandAction(project) {
            val selectionModel = editor.selectionModel
            val startLine: Int
            val endLine: Int

            if (selectionModel.hasSelection()) {
                startLine = document.getLineNumber(selectionModel.selectionStart)
                endLine = document.getLineNumber(selectionModel.selectionEnd)
            } else {
                startLine = editor.caretModel.logicalPosition.line
                endLine = startLine
            }

            // Check if all non-blank lines are commented
            val allCommented = (startLine..endLine).all { line ->
                val text = getLineText(document, line)
                text.isBlank() || isLineCommented(text)
            }

            // Process lines in reverse to preserve line numbers
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

    private fun isRustxFile(name: String): Boolean {
        val ext = name.substringAfterLast('.', "")
        return ext in setOf("rust", "rx", "roo", "🦀", "🐓", "🦘")
    }

    private fun getLineText(document: com.intellij.openapi.editor.Document, line: Int): String {
        val start = document.getLineStartOffset(line)
        val end = document.getLineEndOffset(line)
        return document.getText(TextRange(start, end))
    }

    private fun isLineCommented(lineText: String): Boolean {
        val trimmed = lineText.trimStart()
        if (trimmed.startsWith("//")) return true
        // # comment but not #[ attribute
        return Regex("^\\s{0,2}#(?!\\[)").find(lineText) != null
    }

    private fun commentLine(document: com.intellij.openapi.editor.Document, line: Int, lineText: String) {
        val lineStart = document.getLineStartOffset(line)
        val indent = lineText.takeWhile { it == ' ' || it == '\t' }
        document.insertString(lineStart + indent.length, "// ")
    }

    private fun uncommentLine(document: com.intellij.openapi.editor.Document, line: Int, lineText: String) {
        val lineStart = document.getLineStartOffset(line)

        // Pattern 1: "// " or "//" at any indentation
        val slashMatch = Regex("^(\\s*)// ?").find(lineText)
        if (slashMatch != null) {
            val prefixEnd = slashMatch.range.last + 1
            document.deleteString(lineStart + slashMatch.groupValues[1].length, lineStart + prefixEnd)
            return
        }

        // Pattern 2: "# " or "#" only within first 2 spaces, but not #[ attributes
        val hashMatch = Regex("^(\\s{0,2})#(?!\\[) ?").find(lineText)
        if (hashMatch != null) {
            val indent = hashMatch.groupValues[1]
            val prefixEnd = hashMatch.range.last + 1
            document.deleteString(lineStart + indent.length, lineStart + prefixEnd)
        }
    }
}
