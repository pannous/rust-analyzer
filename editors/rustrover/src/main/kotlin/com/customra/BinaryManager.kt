package com.customra

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.system.CpuArch
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.PosixFilePermission

@Service(Service.Level.APP)
class BinaryManager {
    private val log = Logger.getInstance(BinaryManager::class.java)

    private val installDir: Path = Path.of(System.getProperty("user.home"))
        .resolve(".local/share/custom-rust-analyzer")

    private val binaryName = "rust-analyzer"

    fun getBinaryPath(): Path {
        val binaryPath = installDir.resolve(binaryName)
        // Always extract to ensure we have the latest version from the plugin
        extractBinary(binaryPath)
        return binaryPath
    }

    private fun extractBinary(targetPath: Path) {
        val arch = if (CpuArch.isArm64()) "arm64" else "x64"
        val resourcePath = "/bin/macos-$arch/$binaryName"

        log.info("Extracting custom rust-analyzer ($arch) to $targetPath")

        Files.createDirectories(targetPath.parent)

        javaClass.getResourceAsStream(resourcePath)?.use { input ->
            Files.copy(input, targetPath, StandardCopyOption.REPLACE_EXISTING)
        } ?: throw IllegalStateException("Binary not found in plugin resources: $resourcePath")

        Files.setPosixFilePermissions(targetPath, setOf(
            PosixFilePermission.OWNER_READ,
            PosixFilePermission.OWNER_WRITE,
            PosixFilePermission.OWNER_EXECUTE
        ))

        log.info("Custom rust-analyzer extracted successfully")
    }

    companion object {
        fun getInstance(): BinaryManager =
            ApplicationManager.getApplication().getService(BinaryManager::class.java)
    }
}
