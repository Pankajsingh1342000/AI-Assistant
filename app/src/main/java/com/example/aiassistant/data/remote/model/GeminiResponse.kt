package com.example.aiassistant.data.remote.model

data class GeminiResponse(
    val candidates: List<Candidate>?
)

data class Candidate(
    val content: ContentData
)

data class ContentData(
    val role: String,
    val parts: List<PartResponse>
)

data class PartResponse(
    val text: String
)
