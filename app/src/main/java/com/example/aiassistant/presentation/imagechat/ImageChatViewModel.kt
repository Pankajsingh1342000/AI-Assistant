package com.example.aiassistant.presentation.imagechat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aiassistant.data.manager.TTSManager
import com.example.aiassistant.data.manager.VoiceInputManager
import com.example.aiassistant.domain.usecase.CaptureImageUseCase
import com.example.aiassistant.domain.usecase.SendToGeminiUseCase
import com.example.aiassistant.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ImageChatViewModel @Inject constructor(
    private val voiceInputManager: VoiceInputManager,
    private val ttsManager: TTSManager,
    private val sendToGeminiUseCase: SendToGeminiUseCase,
    private val captureImageUseCase: CaptureImageUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<ImageChatUiState>(ImageChatUiState.Idle)
    val uiState: StateFlow<ImageChatUiState> = _uiState

    fun startVoiceAndImageFlow(imageFile: File) {
        _uiState.value = ImageChatUiState.Listening
        voiceInputManager.startListening(
            onResult = { spokenText ->
                _uiState.value = ImageChatUiState.Processing(spokenText)
                captureAndSend(imageFile, spokenText)
            },
            onError = { error ->
                _uiState.value = ImageChatUiState.Error(error)
            }
        )
    }

    private fun captureAndSend(file: File, query: String) {
        _uiState.value = ImageChatUiState.Capturing
        captureImageUseCase.captureAndEncode(file,
            onResult = { base64 ->
                askGemini(query, base64)
            },
            onError = { error ->
                _uiState.value = ImageChatUiState.Error(error)
            }
        )
    }

    private fun askGemini(query: String, base64Image: String?) {
        viewModelScope.launch {
            _uiState.value = ImageChatUiState.Loading
            val result = sendToGeminiUseCase(query, base64Image)
            when (result) {
                is Resource.Success -> {

                    val responseText = result.data
                    _uiState.value = ImageChatUiState.Success(responseText)

                    ttsManager.speak(responseText,
                        onStart = {
                            // Speech started
                        },
                        onWordRange = { start, end ->
                            _uiState.value = ImageChatUiState.HighlightByRange(responseText, start, end)
                        }
                    )

                }
                is Resource.Error -> {
                    _uiState.value = ImageChatUiState.Error(result.message ?: "Unknown error")
                }
                else -> Unit
            }
        }
    }

    fun setAmplitudeListener(listener: (Int) -> Unit) {
        voiceInputManager.setAmplitudeListener(listener)
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
        voiceInputManager.destroy()
    }
}