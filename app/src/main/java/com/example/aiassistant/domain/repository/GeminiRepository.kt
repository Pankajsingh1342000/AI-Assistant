package com.example.aiassistant.domain.repository

interface GeminiRepository {
    suspend fun sendQuery(query: String, base64Image: String?): String
}