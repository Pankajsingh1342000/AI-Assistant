package com.example.aiassistant.presentation.imagechat

sealed class ImageChatUiState {
    object Idle : ImageChatUiState()
    object Listening : ImageChatUiState()
    object Capturing : ImageChatUiState()
    object Loading : ImageChatUiState()
    data class Processing(val input: String) : ImageChatUiState()
    data class Success(val response: String) : ImageChatUiState()
    data class Error(val message: String) : ImageChatUiState()
    data class HighlightByRange(val response: String, val start: Int, val end: Int) : ImageChatUiState()
}