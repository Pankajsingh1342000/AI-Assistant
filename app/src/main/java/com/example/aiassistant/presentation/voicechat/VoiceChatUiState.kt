package com.example.aiassistant.presentation.voicechat

sealed class VoiceChatUiState {
    object Idle : VoiceChatUiState()
    object Listening : VoiceChatUiState()
    data class Processing(val query: String) : VoiceChatUiState()
    object Loading : VoiceChatUiState()
    data class Success(val response: String) : VoiceChatUiState()
    data class Error(val message: String) : VoiceChatUiState()
    data class HighlightByRange(val response: String, val start: Int, val end: Int) : VoiceChatUiState()
}