package com.example.aiassistant.data.manager

import android.speech.tts.TextToSpeech
import javax.inject.Inject

class TTSManager @Inject constructor(
    private val tts: TextToSpeech
) {
    fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun shutdown(){
        tts.stop()
        tts.shutdown()
    }
}