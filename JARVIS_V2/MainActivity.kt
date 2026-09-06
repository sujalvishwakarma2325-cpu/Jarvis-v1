package com.jarvis.v1

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

import com.jarvis.v1.ai.GeminiAI
import com.jarvis.v1.memory.ConversationMemory
import com.jarvis.v1.phone.PhoneControl
import com.jarvis.v1.system.SystemInfo
import com.jarvis.v1.voice.VoiceInput
import com.jarvis.v1.voice.VoiceOutput

class MainActivity : ComponentActivity() {

    private lateinit var status: TextView
    private lateinit var response: TextView

    private lateinit var voiceInput: VoiceInput
    private lateinit var voiceOutput: VoiceOutput
    private lateinit var geminiAI: GeminiAI
    private lateinit var phoneControl: PhoneControl
    private lateinit var systemInfo: SystemInfo
    private lateinit var memory: ConversationMemory

    private val permissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {
                startListening()
            } else {
                status.text = "Microphone permission required."
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createInterface()

        voiceOutput = VoiceOutput(this)

        voiceInput = VoiceInput(
            context = this,

            onResult = { command ->
                handleCommand(command)
            },

            onStatus = { currentStatus ->
                runOnUiThread {
                    status.text = currentStatus
                }
            }
        )

        geminiAI = GeminiAI()
        phoneControl = PhoneControl(this)
        systemInfo = SystemInfo(this)
        memory = ConversationMemory()
    }

    private fun createInterface() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 60, 40, 40)
            setBackgroundColor(
                Color.rgb(5, 7, 13)
            )
        }

        val title = TextView(this).apply {
            text = "J A R V I S"
            textSize = 32f
            setTextColor(
                Color.rgb(93, 231, 255)
            )
        }

        status = TextView(this).apply {
            text = "ONLINE • CORE V2"
            textSize = 14f
            setTextColor(
                Color.rgb(139, 166, 184)
            )
            setPadding(0, 16, 0, 24)
        }

        response = TextView(this).apply {
            text = "Good morning. I am ready for your command."
            textSize = 20f
            setTextColor(
                Color.rgb(232, 247, 255)
            )
            setPadding(0, 30, 0, 30)
        }

        val listenButton = Button(this).apply {
            text = "🎙  TALK TO JARVIS"
            textSize = 18f

            setOnClickListener {
                checkMicrophonePermission()
            }
        }

        val batteryButton = Button(this).apply {
            text = "CHECK BATTERY"

            setOnClickListener {
                val reply =
                    systemInfo.getBatteryInfo()

                showReply(reply)
            }
        }

        val systemButton = Button(this).apply {
            text = "SYSTEM INFO"

            setOnClickListener {
                val reply =
                    systemInfo.getSystemSummary()

                showReply(reply)
            }
        }

        root.addView(title)
        root.addView(status)
        root.addView(response)
        root.addView(listenButton)
        root.addView(batteryButton)
        root.addView(systemButton)

        setContentView(root)
    }

    private fun checkMicrophonePermission() {

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            startListening()

        } else {

            permissionLauncher.launch(
                Manifest.permission.RECORD_AUDIO
            )
        }
    }

    private fun startListening() {

        status.text = "LISTENING..."

        voiceInput.listen()
    }

    private fun handleCommand(command: String) {

        runOnUiThread {

            status.text = "COMMAND RECEIVED"

            response.text =
                "You: $command"
        }

        val lower =
            command.lowercase()

        memory.addUserMessage(command)

        when {

            "battery" in lower -> {

                val reply =
                    systemInfo.getBatteryInfo()

                showReply(reply)
            }

            "android version" in lower -> {

                val reply =
                    systemInfo.getAndroidVersion()

                showReply(reply)
            }

            "device" in lower ||
                    "phone model" in lower -> {

                val reply =
                    systemInfo.getDeviceInfo()

                showReply(reply)
            }

            "system information" in lower ||
                    "system info" in lower -> {

                val reply =
                    systemInfo.getSystemSummary()

                showReply(reply)
            }

            "open settings" in lower -> {

                phoneControl.openSettings { reply ->
                    showReply(reply)
                }
            }

            "open calculator" in lower -> {

                phoneControl.openCalculator { reply ->
                    showReply(reply)
                }
            }

            "open phone" in lower -> {

                phoneControl.openPhone { reply ->
                    showReply(reply)
                }
            }

            "hello" in lower ||
                    "hi" in lower ||
                    "hey" in lower -> {

                showReply(
                    "Hello. JARVIS is online and ready."
                )
            }

            "your name" in lower -> {

                showReply(
                    "I am JARVIS, your personal AI assistant."
                )
            }

            else -> {

                askGemini(command)
            }
        }
    }

    private fun askGemini(question: String) {

        status.text = "THINKING..."

        geminiAI.ask(
            question
        ) { reply ->

            memory.addAssistantMessage(reply)

            showReply(reply)
        }
    }

    private fun showReply(reply: String) {

        runOnUiThread {

            response.text = reply
            status.text = "JARVIS READY"

            voiceOutput.speak(reply)
        }
    }

    override fun onDestroy() {

        voiceInput.destroy()
        voiceOutput.shutdown()

        super.onDestroy()
    }
}
