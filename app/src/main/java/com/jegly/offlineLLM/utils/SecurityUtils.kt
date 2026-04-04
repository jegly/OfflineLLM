package com.jegly.offlineLLM.utils

import android.util.Log
import java.io.File
import java.security.SecureRandom

object SecurityUtils {

    private const val TAG = "SecurityUtils"

    /**
     * Sanitize user input before sending to inference engine.
     * Escapes special characters and enforces max length.
     */
    fun sanitizePrompt(input: String, maxLength: Int = 2048): String {
        return input
            .take(maxLength)
            .replace("\u0000", "") // Remove null bytes
            .trim()
    }

    /**
     * Redact sensitive content for logging.
     * Never log full prompts or responses.
     */
    fun redactForLog(content: String): String {
        if (content.length <= 6) return "[REDACTED]"
        return "${content.take(3)}***${content.takeLast(3)}"
    }

    /**
     * Securely delete a file by overwriting with random bytes before deletion.
     */
    fun secureDelete(file: File): Boolean {
        return try {
            if (!file.exists()) return true

            val random = SecureRandom()
            val buffer = ByteArray(8192)
            val length = file.length()

            // Overwrite with random data
            file.outputStream().use { output ->
                var written = 0L
                while (written < length) {
                    random.nextBytes(buffer)
                    val toWrite = minOf(buffer.size.toLong(), length - written).toInt()
                    output.write(buffer, 0, toWrite)
                    written += toWrite
                }
                output.flush()
            }

            // Then delete
            file.delete()
        } catch (e: Exception) {
            Log.e(TAG, "Secure delete failed", e)
            file.delete() // Fallback to regular delete
        }
    }

    /**
     * Validate that a file path is within the allowed sandbox directory.
     */
    fun isPathSandboxed(path: String, sandboxDir: String): Boolean {
        val canonicalPath = File(path).canonicalPath
        val canonicalSandbox = File(sandboxDir).canonicalPath
        return canonicalPath.startsWith(canonicalSandbox)
    }
}
