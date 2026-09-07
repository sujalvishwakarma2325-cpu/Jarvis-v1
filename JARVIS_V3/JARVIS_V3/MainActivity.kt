package com.jarvis.v1

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.jarvis.v3.ui.JarvisUI
import com.jarvis.v3.voice.VoiceInput

class MainActivity : ComponentActivity() {

    private lateinit var voiceInput: VoiceInput

    private val microphonePermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {
                voiceInput.startListening()
            } else {
                Toast.makeText(
                    this,
                    "Microphone permission denied.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        voiceInput = VoiceInput(
            context = this,
            listener = object : VoiceInput.Listener {

                override fun onListeningStarted() {
                    Toast.makeText(
                        this@MainActivity,
                        "JARVIS is listening...",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onResult(text: String) {
                    Toast.makeText(
                        this@MainActivity,
                        "You: $text",
                        Toast.LENGTH_LONG
                    ).show()
                }

                override fun onError(message: String) {
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

        val screen = JarvisUI.createMainScreen(
            context = this,
            onTalkClick = {
                startVoiceInput()
            }
        )

        setContentView(screen)
    }

    private fun startVoiceInput() {

        val permission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {

            microphonePermissionLauncher.launch(
                Manifest.permission.RECORD_AUDIO
            )

            return
        }

        voiceInput.startListening()
    }

    override fun onDestroy() {
        voiceInput.destroy()
        super.onDestroy()
    }
}
