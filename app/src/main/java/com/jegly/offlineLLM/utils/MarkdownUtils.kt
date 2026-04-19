package com.jegly.offlineLLM.utils

fun preprocessForMath(text: String): String {
    var processed = text
    // Block math: $$...$$ to code block with math label
    processed = processed.replace(Regex("""\$\$(.*?)\$\$""", RegexOption.DOT_MATCHES_ALL)) {
        "```math\n${it.groupValues[1].trim()}\n```"
    }
    // Inline math: $...$ to inline code
    processed = processed.replace(Regex("""(?<!\\)\$(.+?)\$(?!\\)""")) {
        "`${it.groupValues[1]}`"
    }
    return processed
}
