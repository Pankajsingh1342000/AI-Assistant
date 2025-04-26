package com.example.aiassistant.data.manager

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import javax.inject.Inject

class TTSManager @Inject constructor(
    private val tts: TextToSpeech
) {
    private var onSpeechStarted: (() -> Unit)? = null
    private var onWordRange: ((Int, Int) -> Unit)? = null

    init {
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                if (utteranceId == "tts1") {
                    onSpeechStarted?.invoke()
                }
            }

            override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                if (utteranceId == "tts1") {
                    onWordRange?.invoke(start, end)
                }
            }

            override fun onDone(utteranceId: String?) {}
            override fun onError(utteranceId: String?) {}
        })
    }

    fun speak(text: String, onStart: (() -> Unit)? = null, onWordRange: ((Int, Int) -> Unit)? = null) {
        this.onSpeechStarted = onStart
        this.onWordRange = onWordRange
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts1")
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }
}