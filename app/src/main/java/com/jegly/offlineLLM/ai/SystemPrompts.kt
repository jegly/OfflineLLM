package com.jegly.offlineLLM.ai

object SystemPrompts {
    data class PromptOption(
        val key: String,
        val label: String,
        val prompt: String
    )

    data class Language(val code: String, val label: String)

    val languages = listOf(
        Language("af", "Afrikaans"),
        Language("sq", "Albanian"),
        Language("ar", "Arabic"),
        Language("hy", "Armenian"),
        Language("az", "Azerbaijani"),
        Language("eu", "Basque"),
        Language("be", "Belarusian"),
        Language("bn", "Bengali"),
        Language("bs", "Bosnian"),
        Language("bg", "Bulgarian"),
        Language("ca", "Catalan"),
        Language("zh", "Chinese (Simplified)"),
        Language("zh-TW", "Chinese (Traditional)"),
        Language("hr", "Croatian"),
        Language("cs", "Czech"),
        Language("da", "Danish"),
        Language("nl", "Dutch"),
        Language("en", "English"),
        Language("eo", "Esperanto"),
        Language("et", "Estonian"),
        Language("fi", "Finnish"),
        Language("fr", "French"),
        Language("gl", "Galician"),
        Language("ka", "Georgian"),
        Language("de", "German"),
        Language("el", "Greek"),
        Language("gu", "Gujarati"),
        Language("ht", "Haitian Creole"),
        Language("he", "Hebrew"),
        Language("hi", "Hindi"),
        Language("hu", "Hungarian"),
        Language("is", "Icelandic"),
        Language("id", "Indonesian"),
        Language("ga", "Irish"),
        Language("it", "Italian"),
        Language("ja", "Japanese"),
        Language("kn", "Kannada"),
        Language("kk", "Kazakh"),
        Language("ko", "Korean"),
        Language("lv", "Latvian"),
        Language("lt", "Lithuanian"),
        Language("mk", "Macedonian"),
        Language("ms", "Malay"),
        Language("ml", "Malayalam"),
        Language("mt", "Maltese"),
        Language("mr", "Marathi"),
        Language("mn", "Mongolian"),
        Language("ne", "Nepali"),
        Language("nb", "Norwegian"),
        Language("fa", "Persian"),
        Language("pl", "Polish"),
        Language("pt", "Portuguese"),
        Language("pa", "Punjabi"),
        Language("ro", "Romanian"),
        Language("ru", "Russian"),
        Language("sr", "Serbian"),
        Language("si", "Sinhala"),
        Language("sk", "Slovak"),
        Language("sl", "Slovenian"),
        Language("so", "Somali"),
        Language("es", "Spanish"),
        Language("sw", "Swahili"),
        Language("sv", "Swedish"),
        Language("tl", "Tagalog"),
        Language("ta", "Tamil"),
        Language("te", "Telugu"),
        Language("th", "Thai"),
        Language("tr", "Turkish"),
        Language("uk", "Ukrainian"),
        Language("ur", "Urdu"),
        Language("uz", "Uzbek"),
        Language("vi", "Vietnamese"),
        Language("cy", "Welsh"),
        Language("yi", "Yiddish"),
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
            key = "translator",
            label = "Translator",
            prompt = ""
        ),
        PromptOption(
            key = "custom",
            label = "Custom",
            prompt = ""
        ),
    )

    fun buildTranslatorPrompt(fromCode: String, toCode: String): String {
        val from = languages.find { it.code == fromCode }?.label ?: "English"
        val to = languages.find { it.code == toCode }?.label ?: "Spanish"
        return "You are a professional translator. Translate everything the user writes from $from into $to. Output only the translation with no explanations, notes, or commentary."
    }

    fun getPrompt(key: String, customPrompt: String = "", translatorFrom: String = "en", translatorTo: String = "es"): String {
        return when (key) {
            "custom" -> customPrompt
            "translator" -> buildTranslatorPrompt(translatorFrom, translatorTo)
            else -> options.find { it.key == key }?.prompt ?: options[0].prompt
        }
    }

    fun getLabel(key: String): String {
        return options.find { it.key == key }?.label ?: "General Assistant"
    }
}
