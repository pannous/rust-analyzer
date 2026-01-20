package com.customra

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.tree.IElementType

class RustxSyntaxHighlighter : SyntaxHighlighterBase() {

    companion object {
        val COMMENT = TextAttributesKey.createTextAttributesKey(
            "RUSTX_COMMENT",
            DefaultLanguageHighlighterColors.LINE_COMMENT
        )

        private val COMMENT_KEYS = arrayOf(COMMENT)
        private val EMPTY_KEYS = emptyArray<TextAttributesKey>()
    }

    override fun getHighlightingLexer(): Lexer = RustxLexer()

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> {
        return when (tokenType) {
            RustxTokenTypes.HASH_COMMENT -> COMMENT_KEYS
            else -> EMPTY_KEYS
        }
    }
}

class RustxSyntaxHighlighterFactory : SyntaxHighlighterFactory() {
    override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?): SyntaxHighlighter {
        return RustxSyntaxHighlighter()
    }
}
