package com.jarvis.v3.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

class VoiceInput(
    private val context: Context,
    private val listener: Listener
) {

    interface Listener {
        fun onListeningStarted()
        fun onResult(text: String)
        fun onError(message: String)
        fun onListeningStopped()
    }

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false

    init {
        createRecognizer()
    }

    private fun createRecognizer() {

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            listener.onError(
                "Speech recognition is not available on this phone."
            )
            return
        }

        speechRecognizer =
            SpeechRecognizer.createSpeechRecognizer(context)

        speechRecognizer?.setRecognitionListener(
            object : RecognitionListener {

                override fun onReadyForSpeech(params: Bundle?) {
                    listener.onListeningStarted()
                }

                override fun onBeginningOfSpeech() {
                }

                override fun onRmsChanged(rmsdB: Float) {
                }

                override fun onBufferReceived(buffer: ByteArray?) {
                }

                override fun onEndOfSpeech() {
                    isListening = false
                    listener.onListeningStopped()
                }

                override fun onError(error: Int) {

                    isListening = false

                    val message =
                        when (error) {

                            SpeechRecognizer.ERROR_AUDIO ->
                                "Microphone audio error."

                            SpeechRecognizer.ERROR_CLIENT ->
                                "Speech recognizer client error."

                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                                "Microphone permission is required."

                            SpeechRecognizer.ERROR_NETWORK ->
                                "Speech network error."

                            SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                                "Speech network timeout."

                            SpeechRecognizer.ERROR_NO_MATCH ->
                                "I couldn't understand what you said. Please speak again."

                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY ->
                                "Speech recognizer is busy. Please try again."

                            SpeechRecognizer.ERROR_SERVER ->
                                "Speech server error. Please try again."

                            SpeechRecognizer.ERROR_SERVER_DISCONNECTED ->
                                "Speech server disconnected. Retrying..."

                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                                "I didn't hear anything. Please speak again."

                            else ->
                                "Speech recognition error: $error"
                        }

                    listener.onError(message)

                    // Automatically recreate recognizer after server disconnect.
                    if (
                        error ==
                        SpeechRecognizer.ERROR_SERVER_DISCONNECTED
                    ) {
                        recreateRecognizer()
                    }
                }

                override fun onResults(results: Bundle?) {

                    isListening = false

                    val matches =
                        results?.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION
                        )

                    val text =
                        matches
                            ?.firstOrNull()
                            ?.trim()
                            ?: ""

                    if (text.isNotBlank()) {
                        listener.onResult(text)
                    } else {
                        listener.onError(
                            "I couldn't understand your speech."
                        )
                    }
                }

                override fun onPartialResults(
                    partialResults: Bundle?
                ) {
                }

                override fun onEvent(
                    eventType: Int,
                    params: Bundle?
                ) {
                }
            }
        )
    }

    fun startListening() {

        if (isListening) {
            return
        }

        if (speechRecognizer == null) {
            createRecognizer()
        }

        val intent =
            Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH
            ).apply {

                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )

                // Better for Indian English/Hinglish.
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE,
                    "en-IN"
                )

                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                    "en-IN"
                )

                putExtra(
                    RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                    false
                )

                putExtra(
                    RecognizerIntent.EXTRA_MAX_RESULTS,
                    3
                )

                putExtra(
                    RecognizerIntent.EXTRA_CALLING_PACKAGE,
                    context.packageName
                )
            }

        try {

            isListening = true

            speechRecognizer?.startListening(intent)

        } catch (e: Exception) {

            isListening = false

            listener.onError(
                "Unable to start speech recognition: ${
                    e.message ?: "Unknown error"
                }"
            )
        }
    }

    private fun recreateRecognizer() {

        try {
            speechRecognizer?.destroy()
        } catch (_: Exception) {
        }

        speechRecognizer = null

        android.os.Handler(
            android.os.Looper.getMainLooper()
        ).postDelayed(
            {
                createRecognizer()
            },
            1000L
        )
    }

    fun destroy() {

        isListening = false

        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) {
        }

        try {
            speechRecognizer?.cancel()
        } catch (_: Exception) {
        }

        try {
            speechRecognizer?.destroy()
        } catch (_: Exception) {
        }

        speechRecognizer = null
    }
}
