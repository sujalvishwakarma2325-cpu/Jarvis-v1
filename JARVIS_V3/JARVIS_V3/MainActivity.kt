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

                Toast.makeText(
                    this,
                    "Microphone permission denied.",
                    Toast.LENGTH_LONG
                ).show()
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

                            Toast.makeText(
                                this@MainActivity,
                                "JARVIS is listening...",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        override fun onResult(
                            text: String
                        ) {

                            Toast.makeText(
                                this@MainActivity,
                                "You: $text",
                                Toast.LENGTH_LONG
                            ).show()

                            askGemini(text)
                        }

                        override fun onError(
                            message: String
                        ) {

                            Toast.makeText(
                                this@MainActivity,
                                message,
                                Toast.LENGTH_LONG
                            ).show()
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

        Toast.makeText(
            this,
            "JARVIS is thinking...",
            Toast.LENGTH_SHORT
        ).show()

        activityScope.launch {

            val result =
                geminiAI.askJarvis(text)

            result.onSuccess { response ->

                Toast.makeText(
                    this@MainActivity,
                    "JARVIS: $response",
                    Toast.LENGTH_LONG
                ).show()

                // JARVIS speaks the AI response.
                voiceOutput.speak(response)
            }

            result.onFailure { error ->

                val errorMessage =
                    error.message
                        ?: "Unknown AI error"

                Toast.makeText(
                    this@MainActivity,
                    "AI Error: $errorMessage",
                    Toast.LENGTH_LONG
                ).show()

                voiceOutput.speak(
                    "Sorry, I am having trouble connecting to my AI service."
                )
            }
        }
    }

    override fun onDestroy() {

        voiceInput.destroy()
        voiceOutput.destroy()

        activityJob.cancel()

        super.onDestroy()
    }
}
