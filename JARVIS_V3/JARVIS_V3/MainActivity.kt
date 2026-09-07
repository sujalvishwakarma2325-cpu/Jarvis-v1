package com.jarvis.v1

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.jarvis.v3.ui.JarvisUI
import com.jarvis.v3.voice.VoiceInput

class MainActivity : ComponentActivity() {

    private lateinit var voiceInput: VoiceInput

    companion object {
        private const val RECORD_AUDIO_REQUEST = 1001
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

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RECORD_AUDIO_REQUEST
            )
            return
        }

        voiceInput.startListening()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (requestCode == RECORD_AUDIO_REQUEST) {

            if (
                grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                voiceInput.startListening()
            } else {
                Toast.makeText(
                    this,
                    "Microphone permission denied.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroy() {
        voiceInput.destroy()
        super.onDestroy()
    }
}
