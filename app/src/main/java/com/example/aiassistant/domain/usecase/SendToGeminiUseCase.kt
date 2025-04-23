package com.example.aiassistant.domain.usecase

import com.example.aiassistant.data.remote.GeminiApiService
import com.example.aiassistant.data.remote.model.Content
import com.example.aiassistant.data.remote.model.GeminiRequest
import com.example.aiassistant.data.remote.model.Part
import com.example.aiassistant.utils.Constants
import com.example.aiassistant.utils.Resource
import javax.inject.Inject

class SendToGeminiUseCase @Inject constructor(
    private val apiService: GeminiApiService
) {
    suspend operator fun invoke(query: String): Resource<String> {
        return try {
            val request = GeminiRequest(
                contents = listOf(
                    Content(
                        parts = listOf(
                            Part(text = query) // Only voice input used now
                        )
                    )
                )
            )

            val response = apiService.generateContent(Constants.GEMINI_API_KEY, request)

            val text = response.candidates
                .firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text ?: "No response"

            Resource.Success(text)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Something went wrong")
        }
    }
}