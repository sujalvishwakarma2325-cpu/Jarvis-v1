package com.jarvis.v3.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class VoiceOutput(
    context: Context
) : TextToSpeech.OnInitListener {

    private val textToSpeech: TextToSpeech =
        TextToSpeech(context.applicationContext, this)

    private var ready = false

    override fun onInit(status: Int) {

        if (
            status ==
            TextToSpeech.SUCCESS
        ) {

            val result =
                textToSpeech.setLanguage(
                    Locale("en", "IN")
                )

            ready =
                result != TextToSpeech.LANG_MISSING_DATA &&
                result != TextToSpeech.LANG_NOT_SUPPORTED

            textToSpeech.setSpeechRate(0.95f)
            textToSpeech.setPitch(1.0f)
        }
    }

    fun speak(text: String) {

        if (!ready || text.isBlank()) {
            return
        }

        textToSpeech.stop()

        textToSpeech.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "JARVIS_RESPONSE"
        )
    }

    fun stop() {
        textToSpeech.stop()
    }

    fun destroy() {
        textToSpeech.stop()
        textToSpeech.shutdown()
    }
}
