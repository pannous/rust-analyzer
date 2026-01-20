package com.customra

import com.intellij.lexer.LexerBase
import com.intellij.psi.tree.IElementType

/**
 * Simple lexer that identifies # comments (with space before and after: " # ")
 * All other text is returned as regular content.
 */
class RustxLexer : LexerBase() {
    private var buffer: CharSequence = ""
    private var startOffset = 0
    private var endOffset = 0
    private var currentPos = 0
    private var tokenStart = 0
    private var tokenEnd = 0
    private var currentToken: IElementType? = null

    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        this.buffer = buffer
        this.startOffset = startOffset
        this.endOffset = endOffset
        this.currentPos = startOffset
        advance()
    }

    override fun getState(): Int = 0

    override fun getTokenType(): IElementType? = currentToken

    override fun getTokenStart(): Int = tokenStart

    override fun getTokenEnd(): Int = tokenEnd

    override fun advance() {
        if (currentPos >= endOffset) {
            currentToken = null
            return
        }

        tokenStart = currentPos

        // Check for " # " pattern (hash comment with space before and after)
        if (isHashCommentStart(currentPos)) {
            // Find end of line
            while (currentPos < endOffset && buffer[currentPos] != '\n') {
                currentPos++
            }
            tokenEnd = currentPos
            currentToken = RustxTokenTypes.HASH_COMMENT
            return
        }

        // Regular content - advance to next potential comment or end of buffer
        while (currentPos < endOffset) {
            if (buffer[currentPos] == '\n') {
                currentPos++
                break
            }
            if (isHashCommentStart(currentPos)) {
                break
            }
            currentPos++
        }
        tokenEnd = currentPos
        currentToken = RustxTokenTypes.CODE
    }

    private fun isHashCommentStart(pos: Int): Boolean {
        // Check for " # " - space, hash, space
        if (pos + 2 >= endOffset) return false
        if (pos > startOffset && buffer[pos - 1] == ' ' && buffer[pos] == '#' && buffer[pos + 1] == ' ') {
            return true
        }
        // Also check for line starting with "# " (within 2 spaces)
        val lineStart = findLineStart(pos)
        val indent = pos - lineStart
        if (indent <= 2 && buffer[pos] == '#' && pos + 1 < endOffset && buffer[pos + 1] == ' ') {
            return true
        }
        return false
    }

    private fun findLineStart(pos: Int): Int {
        var p = pos
        while (p > startOffset && buffer[p - 1] != '\n') {
            p--
        }
        return p
    }

    override fun getBufferSequence(): CharSequence = buffer

    override fun getBufferEnd(): Int = endOffset
}

object RustxTokenTypes {
    val CODE = IElementType("CODE", RustxLanguage)
    val HASH_COMMENT = IElementType("HASH_COMMENT", RustxLanguage)
}
