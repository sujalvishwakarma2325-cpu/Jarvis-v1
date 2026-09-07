package com.jarvis.v3.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

object JarvisUI {

    fun createMainScreen(
        context: Context,
        onTalkClick: () -> Unit
    ): LinearLayout {

        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(32, 50, 32, 32)
            setBackgroundColor(JarvisTheme.BACKGROUND)
        }

        val title = TextView(context).apply {
            text = "J A R V I S"
            textSize = 32f
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(JarvisTheme.PRIMARY)
        }

        val subtitle = TextView(context).apply {
            text = "PERSONAL AI ASSISTANT • V3"
            textSize = 12f
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 20)
            setTextColor(JarvisTheme.TEXT_SECONDARY)
        }

        val status = TextView(context).apply {
            text = "●  SYSTEM ONLINE"
            textSize = 15f
            gravity = Gravity.CENTER
            setTextColor(JarvisTheme.ONLINE)
        }

        val orb = JarvisOrbView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                320,
                320
            ).apply {
                gravity = Gravity.CENTER
                topMargin = 20
                bottomMargin = 20
            }
        }

        val message = TextView(context).apply {
            text = "Good to see you.\nI am ready."
            textSize = 20f
            gravity = Gravity.CENTER
            setTextColor(JarvisTheme.TEXT_PRIMARY)
            setPadding(20, 10, 20, 20)
        }

        val talkButton = Button(context).apply {
            text = "🎙  TALK TO JARVIS"
            textSize = 17f
            setTextColor(Color.WHITE)
            setOnClickListener {
                onTalkClick()
            }
        }

        root.addView(title)
        root.addView(subtitle)
        root.addView(status)
        root.addView(orb)
        root.addView(message)
        root.addView(talkButton)

        return root
    }
}
