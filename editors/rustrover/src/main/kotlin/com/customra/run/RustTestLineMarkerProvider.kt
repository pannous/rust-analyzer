package com.customra.run

import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.execution.RunManager
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.runners.ExecutionUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.customra.run.RustRunConfigurationType
import com.customra.run.RustRunConfiguration

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
            return Info(
                AllIcons.RunConfigurations.TestState.Run,
                { "Run ${file.name}" },
                createRunAction(element, null)
            )
        }

        // Show marker on function keyword "fn " when it's a test function
        // Only show on fn, not on #[test] to avoid duplicate markers
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
                    // Extract just the name part
                    val name = nextText.substringBefore("(").substringBefore("{").trim()
                    if (name.isNotEmpty()) {
                        functionName += name
                    }
                    break
                }
                next = next.nextSibling
                steps++
            }

            val cleanName = functionName.substringBefore("(").substringBefore("{").trim()

            // Check if function name starts with test_
            if (cleanName.startsWith("test_")) {
                return Info(
                    AllIcons.RunConfigurations.TestState.Run,
                    { "Run $cleanName" },
                    createRunAction(element, cleanName)
                )
            }

            // Check if there's a #[test] attribute before this function
            var prev = element.prevSibling
            var checkSteps = 0
            while (prev != null && checkSteps < 10) {
                val prevText = prev.text.trim()
                if (prevText == "#[" || prevText.startsWith("test]")) {
                    // Found test attribute nearby
                    if (cleanName.isNotBlank()) {
                        return Info(
                            AllIcons.RunConfigurations.TestState.Run,
                            { "Run $cleanName" },
                            createRunAction(element, cleanName)
                        )
                    }
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

    private fun createRunAction(element: PsiElement, testName: String?): AnAction {
        return object : AnAction() {
            override fun actionPerformed(e: AnActionEvent) {
                val project = e.project ?: return
                val virtualFile = element.containingFile.virtualFile ?: return

                val runManager = RunManager.getInstance(project)
                val factory = RustRunConfigurationType.getInstance().configurationFactories[0]

                val settings = runManager.createConfiguration(
                    "Run ${testName ?: virtualFile.nameWithoutExtension}",
                    factory
                )

                val configuration = settings.configuration as RustRunConfiguration
                configuration.scriptPath = virtualFile.path
                configuration.testFilter = testName ?: ""

                runManager.addConfiguration(settings)
                runManager.selectedConfiguration = settings

                ExecutionUtil.runConfiguration(settings, DefaultRunExecutor.getRunExecutorInstance())
            }
        }
    }

    private fun isRustxFile(file: PsiFile): Boolean {
        val extension = file.virtualFile?.extension ?: return false
        return extension in RustRunConfigurationProducer.RUST_EXTENSIONS
    }
}
