package com.zombiethumb

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File

/**
 * Privacy enforcement tests.
 *
 * These tests verify that the app's privacy guarantees are maintained:
 * 1. No INTERNET permission declared in the manifest
 * 2. No calls to content-reading APIs (event.text, node.text, contentDescription)
 * 3. canRetrieveWindowContent is false in accessibility config
 * 4. No dispatchGesture calls
 */
class PrivacyEnforcementTest {

    /**
     * Verify that AndroidManifest.xml does NOT declare INTERNET permission.
     * The manifest explicitly removes it via tools:node="remove".
     */
    @Test
    fun `manifest does not declare INTERNET permission`() {
        val manifestFile = findFile("src/main/AndroidManifest.xml") ?: findFile("app/src/main/AndroidManifest.xml")
        if (manifestFile != null) {
            val content = manifestFile.readText()
            // Should have tools:node="remove" for INTERNET
            assertThat(content).contains("android.permission.INTERNET")
            assertThat(content).contains("tools:node=\"remove\"")
            // Should NOT have a standalone INTERNET permission grant
            val lines = content.lines()
            val internetLines = lines.filter {
                it.contains("android.permission.INTERNET") && !it.contains("tools:node")
            }
            assertThat(internetLines).isEmpty()
        }
    }

    /**
     * Verify that accessibility_service_config.xml has canRetrieveWindowContent="false".
     */
    @Test
    fun `accessibility config disables window content retrieval`() {
        val configFile = findFile("accessibility_service_config.xml")
        if (configFile != null) {
            val content = configFile.readText()
            assertThat(content).contains("canRetrieveWindowContent=\"false\"")
            assertThat(content).contains("canPerformGestures=\"false\"")
        }
    }

    /**
     * Scan Kotlin source files for forbidden content-reading API calls.
     * These APIs must NEVER be used:
     * - event.text / getText()
     * - node.text / getText()
     * - contentDescription
     * - dispatchGesture
     */
    @Test
    fun `source code does not use forbidden content-reading APIs`() {
        val forbiddenPatterns = listOf(
            "event.text",
            "event.getText()",
            "node.text",
            "node.getText()",
            ".contentDescription",
            "getContentDescription()",
            "dispatchGesture",
            "performAction(AccessibilityNodeInfo.ACTION_SET_TEXT",
        )

        val srcDir = findDir("src/main/java")
        if (srcDir != null) {
            val violations = mutableListOf<String>()
            srcDir.walkTopDown()
                .filter { it.isFile && it.extension == "kt" }
                .forEach { file ->
                    val lines = file.readLines()
                    lines.forEachIndexed { lineNum, line ->
                        // Skip comments
                        val trimmed = line.trim()
                        if (trimmed.startsWith("//") || trimmed.startsWith("*") || trimmed.startsWith("/*")) {
                            return@forEachIndexed
                        }
                        forbiddenPatterns.forEach { pattern ->
                            if (line.contains(pattern)) {
                                violations.add("${file.name}:${lineNum + 1} — contains '$pattern'")
                            }
                        }
                    }
                }

            com.google.common.truth.Truth.assertWithMessage("Privacy violation found:\n${violations.joinToString("\n")}")
                .that(violations)
                .isEmpty()
        }
    }

    // Helper to find files relative to working directory
    private fun findFile(relativePath: String): File? {
        val cwd = File(System.getProperty("user.dir") ?: ".")
        val file = File(cwd, relativePath)
        if (file.exists()) return file
        // Also check if we're running from root instead of app/
        val appFile = File(cwd, "app/$relativePath")
        if (appFile.exists()) return appFile
        
        // Fallback for just the name
        return cwd.walkTopDown().firstOrNull { it.name == relativePath }
    }

    private fun findDir(relativePath: String): File? {
        val cwd = File(System.getProperty("user.dir") ?: ".")
        val dir = File(cwd, relativePath)
        return if (dir.exists() && dir.isDirectory) dir else null
    }
}
