package com.jarvis.v3.voice

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat
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

    private var speechRecognizer:
        SpeechRecognizer? = null

    private var isListening = false

    fun startListening() {

        if (
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            listener.onError(
                "Microphone permission is required."
            )

            return
        }

        if (
            !SpeechRecognizer
                .isRecognitionAvailable(context)
        ) {

            listener.onError(
                "Speech recognition is not available."
            )

            return
        }

        stopListening()

        speechRecognizer =
            SpeechRecognizer
                .createSpeechRecognizer(context)

        speechRecognizer?.setRecognitionListener(
            object : RecognitionListener {

                override fun onReadyForSpeech(
                    params: Bundle?
                ) {

                    isListening = true

                    listener.onListeningStarted()
                }

                override fun onBeginningOfSpeech() {
                    isListening = true
                }

                override fun onRmsChanged(
                    rmsdB: Float
                ) {
                }

                override fun onBufferReceived(
                    buffer: ByteArray?
                ) {
                }

                override fun onEndOfSpeech() {

                    isListening = false

                    listener.onListeningStopped()
                }

                override fun onError(
                    error: Int
                ) {

                    isListening = false

                    val message = when (error) {

                        SpeechRecognizer.ERROR_AUDIO ->
                            "Microphone audio error."

                        SpeechRecognizer.ERROR_CLIENT ->
                            "Speech recognition client error."

                        SpeechRecognizer
                            .ERROR_INSUFFICIENT_PERMISSIONS ->
                            "Microphone permission required."

                        SpeechRecognizer.ERROR_NETWORK ->
                            "Speech recognition network error."

                        SpeechRecognizer
                            .ERROR_NETWORK_TIMEOUT ->
                            "Speech recognition network timeout."

                        SpeechRecognizer.ERROR_NO_MATCH ->
                            "Speech not recognised. Please speak clearly."

                        SpeechRecognizer
                            .ERROR_RECOGNIZER_BUSY ->
                            "Speech recognizer is busy."

                        SpeechRecognizer.ERROR_SERVER ->
                            "Speech recognition server error."

                        SpeechRecognizer
                            .ERROR_SPEECH_TIMEOUT ->
                            "No speech detected."

                        else ->
                            "Speech recognition error: $error"
                    }

                    listener.onError(message)
                }

                override fun onResults(
                    results: Bundle?
                ) {

                    isListening = false

                    val matches =
                        results?.getStringArrayList(
                            SpeechRecognizer
                                .RESULTS_RECOGNITION
                        )

                    val text =
                        matches
                            ?.firstOrNull()
                            ?.trim()
                            ?: ""

                    if (text.isNotEmpty()) {
                        listener.onResult(text)
                    } else {
                        listener.onError(
                            "Speech not recognised."
                        )
                    }

                    listener.onListeningStopped()
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

        val intent =
            Intent(
                RecognizerIntent
                    .ACTION_RECOGNIZE_SPEECH
            ).apply {

                putExtra(
                    RecognizerIntent
                        .EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent
                        .LANGUAGE_MODEL_FREE_FORM
                )

                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE,
                    Locale.US
                )

                putExtra(
                    RecognizerIntent
                        .EXTRA_LANGUAGE_PREFERENCE,
                    Locale.US
                )

                putExtra(
                    RecognizerIntent
                        .EXTRA_PARTIAL_RESULTS,
                    true
                )

                putExtra(
                    RecognizerIntent
                        .EXTRA_MAX_RESULTS,
                    3
                )

                putExtra(
                    RecognizerIntent
                        .EXTRA_PROMPT,
                    "Speak to JARVIS"
                )
            }

        speechRecognizer
            ?.startListening(intent)
    }

    fun stopListening() {

        speechRecognizer
            ?.stopListening()

        speechRecognizer
            ?.cancel()

        speechRecognizer
            ?.destroy()

        speechRecognizer = null

        isListening = false
    }

    fun isCurrentlyListening(): Boolean {
        return isListening
    }

    fun destroy() {
        stopListening()
    }
}
