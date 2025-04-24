package com.example.aiassistant.data.repository

import com.example.aiassistant.data.remote.GeminiApiService
import com.example.aiassistant.data.remote.model.Content
import com.example.aiassistant.data.remote.model.GeminiRequest
import com.example.aiassistant.data.remote.model.InlineData
import com.example.aiassistant.data.remote.model.Part
import com.example.aiassistant.domain.repository.GeminiRepository
import com.example.aiassistant.utils.Constants
import com.example.aiassistant.utils.TextCleaner
import javax.inject.Inject

class GeminiRepositoryImpl @Inject constructor(
    private val api: GeminiApiService
) : GeminiRepository {

    override suspend fun sendQuery(query: String, base64Image: String?): String {
        val parts = mutableListOf<Part>()

        if (!base64Image.isNullOrBlank()) {
            parts.add(
                Part(
                    inline_data = InlineData(
                        mime_type = "image/jpeg",
                        data = base64Image
                    )
                )
            )
        }

        parts.add(Part(text = query))

        val request = GeminiRequest(
            contents = listOf(Content(role = "user", parts = parts))
        )

        val response = api.generateContent(Constants.GEMINI_API_KEY, request)

        val rawText = response.candidates
            ?.firstOrNull()
            ?.content
            ?.parts
            ?.firstOrNull()
            ?.text ?: "No response"

        return TextCleaner.cleanGeminiResponse(rawText)
    }
}