package com.jegly.offlineLLM.ai

import com.jegly.offlineLLM.data.local.entities.Message

object PromptFormatter {

    private const val MAX_PROMPT_LENGTH = 2048

    fun sanitizeInput(input: String): String {
        return input
            .take(MAX_PROMPT_LENGTH)
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .trim()
    }

    fun buildConversationContext(
        systemPrompt: String,
        messages: List<Message>,
        maxContextTokens: Int = 4096
    ): List<Pair<String, String>> {
        val context = mutableListOf<Pair<String, String>>()

        if (systemPrompt.isNotBlank()) {
            context.add("system" to systemPrompt)
        }

        // Rough token estimation: ~4 chars per token
        var estimatedTokens = systemPrompt.length / 4
        val maxTokenBudget = maxContextTokens * 4 // convert to chars

        // Add messages in reverse order, trimming oldest if over budget
        val recentMessages = messages.reversed().takeWhile { msg ->
            estimatedTokens += msg.content.length / 4
            estimatedTokens < maxTokenBudget
        }.reversed()

        for (msg in recentMessages) {
            context.add(msg.role to msg.content)
        }

        return context
    }

    fun redactForLog(content: String): String {
        if (content.length <= 10) return "[REDACTED]"
        return "${content.take(3)}...[REDACTED]...${content.takeLast(3)}"
    }
}
