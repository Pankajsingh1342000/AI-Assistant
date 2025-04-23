package com.example.aiassistant.presentation.assistant

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
class AssistantViewModel @Inject constructor(
    private val voiceInputManager: VoiceInputManager,
    private val ttsManager: TTSManager,
    private val sendToGeminiUseCase: SendToGeminiUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AssistantUiState>(AssistantUiState.Idle)
    val uiState: StateFlow<AssistantUiState> = _uiState

    fun startVoiceInput() {
        _uiState.value = AssistantUiState.Listening
        voiceInputManager.startListening(
            onResult = { spokenText ->
                _uiState.value = AssistantUiState.Processing(spokenText)
                askGemini(spokenText)
            },
            onError = { error ->
                _uiState.value = AssistantUiState.Error(error)
            }
        )
    }

    private fun askGemini(query: String) {
        viewModelScope.launch {
            _uiState.value = AssistantUiState.Loading
            when (val result = sendToGeminiUseCase(query)) {
                is Resource.Success<*> -> {
                    ttsManager.speak(result.data.toString())
                    _uiState.value = AssistantUiState.Success(result.data.toString())
                }
                is Resource.Error -> {
                    _uiState.value = AssistantUiState.Error(result.message ?: "Unknown error")
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