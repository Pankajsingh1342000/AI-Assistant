package com.example.aiassistant.data.remote

import com.example.aiassistant.data.remote.model.GeminiRequest
import com.example.aiassistant.data.remote.model.GeminiResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface GeminiApiService {

    @POST("v1beta/models/gemini-pro-vision:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse

}