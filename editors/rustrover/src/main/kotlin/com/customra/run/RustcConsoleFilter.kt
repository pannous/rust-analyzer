package com.customra.run

import com.intellij.execution.filters.ConsoleFilterProvider
import com.intellij.execution.filters.Filter
import com.intellij.execution.filters.OpenFileHyperlinkInfo
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import java.io.File

/**
 * Console filter that makes rustc error locations clickable.
 * Matches patterns like: --> /path/to/file.rs:25:84
 */
class RustcConsoleFilterProvider : ConsoleFilterProvider {
    override fun getDefaultFilters(project: Project): Array<Filter> {
        return arrayOf(RustcConsoleFilter(project))
    }
}

class RustcConsoleFilter(private val project: Project) : Filter {

    // Matches: --> /path/to/file.rs:line:column or .rust extension
    // Also matches without arrow: /path/to/file.rs:line:column
    private val pattern = Regex("""(?:-->\s*)?(/[^\s:]+\.rust?):(\d+):(\d+)""")

    override fun applyFilter(line: String, entireLength: Int): Filter.Result? {
        val lineStart = entireLength - line.length
        val results = mutableListOf<Filter.ResultItem>()

        for (match in pattern.findAll(line)) {
            val path = match.groupValues[1]
            val lineNum = match.groupValues[2].toIntOrNull() ?: continue
            val column = match.groupValues[3].toIntOrNull() ?: 1

            val file = File(path)
            if (!file.exists()) continue

            val virtualFile = LocalFileSystem.getInstance().findFileByIoFile(file) ?: continue

            val hyperlinkInfo = OpenFileHyperlinkInfo(
                project,
                virtualFile,
                lineNum - 1,  // 0-indexed
                column - 1    // 0-indexed
            )

            val matchStart = lineStart + match.range.first
            val matchEnd = lineStart + match.range.last + 1

            results.add(Filter.ResultItem(matchStart, matchEnd, hyperlinkInfo))
        }

        return if (results.isNotEmpty()) Filter.Result(results) else null
    }
}
