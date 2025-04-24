package com.example.aiassistant.domain.usecase

import com.example.aiassistant.data.remote.GeminiApiService
import com.example.aiassistant.data.remote.model.Content
import com.example.aiassistant.data.remote.model.GeminiRequest
import com.example.aiassistant.data.remote.model.InlineData
import com.example.aiassistant.data.remote.model.Part
import com.example.aiassistant.utils.Constants
import com.example.aiassistant.utils.Resource
import com.example.aiassistant.utils.TextCleaner
import javax.inject.Inject

class SendToGeminiUseCase @Inject constructor(
    private val apiService: GeminiApiService
) {
    suspend operator fun invoke(query: String, base64Image: String?): Resource<String> {
        return try {
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

            val response = apiService.generateContent(Constants.GEMINI_API_KEY, request)

            val text = response.candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text ?: "No response"

            val cleanedText = TextCleaner.cleanGeminiResponse(text)

            Resource.Success(cleanedText)

        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error("Gemini request failed: ${e.message}")
        }
    }
}