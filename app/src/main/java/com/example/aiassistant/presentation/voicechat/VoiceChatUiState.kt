package com.example.aiassistant.presentation.voicechat

sealed class VoiceChatUiState {
    object Idle : VoiceChatUiState()
    object Listening : VoiceChatUiState()
    object Loading : VoiceChatUiState()
    data class Processing(val input: String) : VoiceChatUiState()
    data class Success(val response: String) : VoiceChatUiState()
    data class Error(val message: String) : VoiceChatUiState()
}