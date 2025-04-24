package com.example.aiassistant.domain.usecase

import com.example.aiassistant.data.remote.GeminiApiService
import com.example.aiassistant.data.remote.model.Content
import com.example.aiassistant.data.remote.model.GeminiRequest
import com.example.aiassistant.data.remote.model.InlineData
import com.example.aiassistant.data.remote.model.Part
import com.example.aiassistant.domain.repository.GeminiRepository
import com.example.aiassistant.utils.Constants
import com.example.aiassistant.utils.Resource
import com.example.aiassistant.utils.TextCleaner
import javax.inject.Inject

class SendToGeminiUseCase @Inject constructor(
    private val repository: GeminiRepository
) {
    suspend operator fun invoke(query: String, base64Image: String?): Resource<String> {
        return try {
            val response = repository.sendQuery(query, base64Image)
            Resource.Success(response)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error("Gemini error: ${e.message}")
        }
    }
}