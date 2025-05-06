package com.example.aiassistant.data.manager

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import javax.inject.Inject

class VoiceInputManager @Inject constructor(
    private val context: Context
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var amplitudeCallback: ((Int) -> Unit)? = null
    private var lastRmsValue = 0f
    private var isListening = false

    fun startListening(onResult: (String) -> Unit, onError: (String) -> Unit) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError("Speech recognition not available")
            return
        }

        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}

            override fun onBeginningOfSpeech() {
                isListening = true
                startAmplitudeUpdates()
            }

            override fun onRmsChanged(rmsdB: Float) {
                lastRmsValue = rmsdB
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                isListening = false
            }

            override fun onError(error: Int) {
                isListening = false
                onError("Speech error: $error")
            }

            override fun onResults(results: Bundle?) {
                isListening = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val spokenText = matches?.firstOrNull() ?: ""
                if (spokenText.isNotEmpty()) {
                    onResult(spokenText)
                } else {
                    onError("No speech result found")
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        isListening = false
        speechRecognizer?.stopListening()
    }

    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    fun setAmplitudeListener(listener: (Int) -> Unit) {
        amplitudeCallback = listener
    }

    private fun startAmplitudeUpdates() {
        Thread {
            while (speechRecognizer != null && isListening) {
                val amp = (lastRmsValue * 3000).toInt()
                amplitudeCallback?.invoke(amp)
                Thread.sleep(50)
            }
        }.start()
    }
}