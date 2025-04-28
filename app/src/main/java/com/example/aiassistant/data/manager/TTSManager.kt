package com.example.aiassistant.data.manager

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class TTSManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var tts: TextToSpeech? = null
    private var isReady = false

    private var onSpeechStarted: (() -> Unit)? = null
    private var onWordRange: ((Int, Int) -> Unit)? = null

    init {
        tts = TextToSpeech(context) { status ->
            isReady = (status == TextToSpeech.SUCCESS)
        }.apply {
            language = java.util.Locale.US
            setOnUtteranceProgressListener(object : UtteranceProgressListener() {
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
    }

    fun speak(
        text: String,
        onStart: (() -> Unit)? = null,
        onWordRange: ((Int, Int) -> Unit)? = null
    ) {
        if (isReady) {
            this.onSpeechStarted = onStart
            this.onWordRange = onWordRange
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts1")
        } else {
            // Optional: Retry logic after 200ms if you want
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}