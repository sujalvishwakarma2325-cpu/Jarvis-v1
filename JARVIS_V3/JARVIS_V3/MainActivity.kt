package com.jarvis.v1

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.jarvis.v3.ai.GeminiAI
import com.jarvis.v3.ui.JarvisUI
import com.jarvis.v3.voice.VoiceInput
import com.jarvis.v3.voice.VoiceOutput
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var voiceInput: VoiceInput
    private lateinit var voiceOutput: VoiceOutput
    private lateinit var geminiAI: GeminiAI

    private val activityJob = Job()

    private val activityScope =
        CoroutineScope(
            Dispatchers.Main + activityJob
        )

    private val microphonePermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {
                startListeningNow()
            } else {
                showMessage(
                    "Microphone permission denied."
                )
            }
        }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        geminiAI = GeminiAI()

        voiceOutput = VoiceOutput(this)

        voiceInput =
            VoiceInput(
                context = this,
                listener =
                    object : VoiceInput.Listener {

                        override fun onListeningStarted() {

                            showMessage(
                                "JARVIS is listening..."
                            )
                        }

                        override fun onResult(
                            text: String
                        ) {

                            showMessage(
                                "You: $text"
                            )

                            askGemini(text)
                        }

                        override fun onError(
                            message: String
                        ) {

                            showMessage(message)
                        }

                        override fun onListeningStopped() {
                        }
                    }
            )

        val screen =
            JarvisUI.createMainScreen(
                context = this,
                onTalkClick = {
                    startVoiceInput()
                }
            )

        setContentView(screen)
    }

    private fun startVoiceInput() {

        val permission =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            )

        if (
            permission !=
            PackageManager.PERMISSION_GRANTED
        ) {

            microphonePermissionLauncher.launch(
                Manifest.permission.RECORD_AUDIO
            )

            return
        }

        startListeningNow()
    }

    private fun startListeningNow() {

        voiceInput.startListening()
    }

    private fun askGemini(
        text: String
    ) {

        showMessage(
            "JARVIS is thinking..."
        )

        activityScope.launch {

            val result =
                geminiAI.askJarvis(text)

            result.onSuccess { response ->

                showMessage(
                    "JARVIS: $response"
                )

                // Speak Gemini's actual answer.
                voiceOutput.speak(response)
            }

            result.onFailure { error ->

                val actualError =
                    error.message
                        ?: "Unknown Gemini error"

                showMessage(
                    "AI Error: $actualError"
                )

                val spokenError =
                    when {

                        actualError.contains(
                            "503"
                        ) ->
                            "Gemini server is temporarily unavailable. Please try again."

                        actualError.contains(
                            "429"
                        ) ->
                            "Gemini request limit has been reached. Please try again later."

                        actualError.contains(
                            "401"
                        ) ->
                            "The Gemini API key is not authorized."

                        actualError.contains(
                            "403"
                        ) ->
                            "Gemini access is not permitted for this API key."

                        actualError.contains(
                            "404"
                        ) ->
                            "The requested Gemini model was not found."

                        actualError.contains(
                            "Network error",
                            ignoreCase = true
                        ) ->
                            "I cannot connect to the Gemini server. Please check your internet connection."

                        else ->
                            "Gemini returned an error. Please try again."
                    }

                voiceOutput.speak(
                    spokenError
                )
            }
        }
    }

    private fun showMessage(
        message: String
    ) {

        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onDestroy() {

        voiceInput.destroy()

        voiceOutput.destroy()

        activityJob.cancel()

        super.onDestroy()
    }
}
