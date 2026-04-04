package com.jegly.offlineLLM.ai

object SystemPrompts {
    data class PromptOption(
        val key: String,
        val label: String,
        val prompt: String
    )

    val options = listOf(
        PromptOption(
            key = "default",
            label = "General Assistant",
            prompt = "You are a helpful, concise AI assistant. Answer questions clearly and directly."
        ),
        PromptOption(
            key = "coder",
            label = "Coder",
            prompt = "You are an expert programming assistant. Write clean, efficient, well-commented code. Explain your reasoning. Suggest best practices and point out potential issues. Support all major programming languages."
        ),
        PromptOption(
            key = "creative",
            label = "Creative Writer",
            prompt = "You are a creative writing assistant with a flair for vivid prose, compelling narratives, and original ideas. Help with stories, poetry, scripts, and other creative works."
        ),
        PromptOption(
            key = "tutor",
            label = "Tutor",
            prompt = "You are a patient and thorough tutor. Explain concepts step by step, use analogies when helpful, and check understanding."
        ),
        PromptOption(
            key = "custom",
            label = "Custom",
            prompt = ""
        ),
    )

    fun getPrompt(key: String, customPrompt: String = ""): String {
        if (key == "custom") return customPrompt
        return options.find { it.key == key }?.prompt ?: options[0].prompt
    }

    fun getLabel(key: String): String {
        return options.find { it.key == key }?.label ?: "General Assistant"
    }
}
