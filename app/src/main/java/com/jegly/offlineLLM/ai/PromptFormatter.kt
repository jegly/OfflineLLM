package com.jegly.offlineLLM.ai

import com.jegly.offlineLLM.data.local.entities.Message

object PromptFormatter {

    private const val MAX_PROMPT_LENGTH = 8192

    fun sanitizeInput(input: String): String {
        return input
            .take(MAX_PROMPT_LENGTH)
            .replace("\u0000", "")
            .trim()
    }

    /**
     * Builds a list of message pairs (role, content) for the inference engine.
     */
    fun buildConversationContext(
        systemPrompt: String,
        messages: List<Message>,
        maxContextTokens: Int = 4096,
        modelName: String = "",
        chatTemplate: String = ""
    ): List<Pair<String, String>> {
        val isGemma = isGemmaModel(modelName, chatTemplate)

        var estimatedTokens = 0
        if (systemPrompt.isNotBlank()) {
            estimatedTokens += systemPrompt.length / 4
        }

        val safetyBuffer = 1024
        val availableBudget = maxOf(512, maxContextTokens - safetyBuffer)

        val recentMessages = messages.reversed().takeWhile { msg ->
            val msgTokens = msg.content.length / 4
            if (estimatedTokens + msgTokens < availableBudget) {
                estimatedTokens += msgTokens
                true
            } else {
                false
            }
        }.reversed()

        val context = mutableListOf<Pair<String, String>>()
        for (msg in recentMessages) {
            val role = if (isGemma && msg.role == "assistant") "model" else msg.role
            context.add(role to msg.content)
        }

        return context
    }

    /**
     * Manually formats a prompt for Gemma 4 using the official tags:
     * Start: <|turn|>role\n
     * End: <turn|>
     */
    fun formatGemma4Prompt(
        systemPrompt: String,
        history: List<Pair<String, String>>,
        currentQuery: String
    ): String {
        val sb = StringBuilder()
        
        // Gemma 4 uses <|turn|> for turn start and <turn|> for turn end
        if (systemPrompt.isNotBlank()) {
            sb.append("<|turn|>system\n").append(systemPrompt.trim()).append("<turn|>\n")
        }

        for ((role, content) in history) {
            val normalizedRole = if (role == "assistant") "model" else role
            sb.append("<|turn|>").append(normalizedRole).append("\n")
            sb.append(content.trim()).append("<turn|>\n")
        }

        // Current user turn
        sb.append("<|turn|>user\n")
        sb.append(currentQuery.trim()).append("<turn|>\n")
        
        // Start model turn
        sb.append("<|turn|>model\n")
        
        return sb.toString()
    }

    fun isGemmaModel(modelName: String, chatTemplate: String): Boolean {
        val lowerName = modelName.lowercase()
        val lowerTmpl = chatTemplate.lowercase()
        return lowerName.contains("gemma") || 
               lowerTmpl.contains("gemma") ||
               lowerTmpl.contains("<turn|") ||
               lowerTmpl.contains("<|turn|>") ||
               lowerTmpl.contains("<start_of_turn>")
    }

    fun isGemma4(modelName: String, chatTemplate: String): Boolean {
        val lowerName = modelName.lowercase()
        return lowerName.contains("gemma4") || 
               lowerName.contains("gemma-4") || 
               lowerName.contains("gemma 4") ||
               chatTemplate.contains("<|turn|>")
    }

    fun redactForLog(content: String): String {
        if (content.length <= 10) return "[REDACTED]"
        return "${content.take(3)}...[REDACTED]...${content.takeLast(3)}"
    }
}
