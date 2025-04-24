package com.example.aiassistant.utils

object TextCleaner {
    fun cleanGeminiResponse(text: String): String {
        return text
            .replace(Regex("""[*_`#>\-]+"""), "") // remove markdown: *, _, #, >, -
            .replace(Regex("""\s+"""), " ")       // remove extra spaces
            .trim()
    }
}