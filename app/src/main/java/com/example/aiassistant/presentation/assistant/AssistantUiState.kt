package com.example.aiassistant.presentation.assistant

sealed class AssistantUiState {
    object Idle : AssistantUiState()
    object Listening : AssistantUiState()
    object Loading : AssistantUiState()
    data class Processing(val input: String) : AssistantUiState()
    data class Success(val response: String) : AssistantUiState()
    data class Error(val message: String) : AssistantUiState()
}