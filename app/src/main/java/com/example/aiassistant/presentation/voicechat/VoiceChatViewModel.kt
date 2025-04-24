package com.example.aiassistant.presentation.voicechat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aiassistant.data.manager.TTSManager
import com.example.aiassistant.data.manager.VoiceInputManager
import com.example.aiassistant.domain.usecase.SendToGeminiUseCase
import com.example.aiassistant.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VoiceChatViewModel @Inject constructor(
    private val voiceInputManager: VoiceInputManager,
    private val ttsManager: TTSManager,
    private val sendToGeminiUseCase: SendToGeminiUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<VoiceChatUiState>(VoiceChatUiState.Idle)
    val uiState: StateFlow<VoiceChatUiState> = _uiState

    fun startVoiceInput() {
        _uiState.value = VoiceChatUiState.Listening
        voiceInputManager.startListening(
            onResult = { spokenText ->
                _uiState.value = VoiceChatUiState.Processing(spokenText)
                askGemini(spokenText)
            },
            onError = { error ->
                _uiState.value = VoiceChatUiState.Error(error)
            }
        )
    }

    private fun askGemini(query: String) {
        viewModelScope.launch {
            _uiState.value = VoiceChatUiState.Loading
            when (val result = sendToGeminiUseCase(query, null)) {
                is Resource.Success -> {
                    ttsManager.speak(result.data)
                    _uiState.value = VoiceChatUiState.Success(result.data)
                }
                is Resource.Error -> {
                    _uiState.value = VoiceChatUiState.Error(result.message ?: "Unknown error")
                }
                else -> Unit
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
        voiceInputManager.destroy()
    }
}
