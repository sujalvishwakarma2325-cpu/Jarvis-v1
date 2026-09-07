package com.jarvis.v3

import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {

    private lateinit var statusText: TextView
    private lateinit var messageText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createJarvisInterface()
    }

    private fun createJarvisInterface() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(40, 40, 40, 40)
            setBackgroundColor(Color.rgb(5, 8, 15))
        }

        val title = TextView(this).apply {
            text = "J A R V I S"
            textSize = 34f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(90, 220, 255))
        }

        val version = TextView(this).apply {
            text = "V3 • CORE INITIALIZING"
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(0, 12, 0, 40)
            setTextColor(Color.rgb(130, 160, 180))
        }

        statusText = TextView(this).apply {
            text = "● SYSTEM ONLINE"
            textSize = 18f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(100, 230, 180))
        }

        messageText = TextView(this).apply {
            text = "Good to see you.\nJARVIS V3 is ready."
            textSize = 20f
            gravity = Gravity.CENTER
            setPadding(20, 40, 20, 40)
            setTextColor(Color.WHITE)
        }

        root.addView(title)
        root.addView(version)
        root.addView(statusText)
        root.addView(messageText)

        setContentView(root)
    }
}
