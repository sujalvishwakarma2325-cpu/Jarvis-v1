package com.jarvis.v3.ui

import android.graphics.Color

object JarvisTheme {

    const val BACKGROUND = 0xFF05080F.toInt()
    const val CARD = 0xFF0B1220.toInt()

    const val PRIMARY = 0xFF5CE7FF.toInt()
    const val PRIMARY_DARK = 0xFF1689A8.toInt()

    const val TEXT_PRIMARY = 0xFFE8F7FF.toInt()
    const val TEXT_SECONDARY = 0xFF8FA6B8.toInt()

    const val ONLINE = 0xFF55E6A5.toInt()
    const val WARNING = 0xFFFFC857.toInt()
    const val ERROR = 0xFFFF6675.toInt()

    const val ORB_GLOW = 0xFF2AC7FF.toInt()

    fun backgroundColor(): Int {
        return Color.rgb(5, 8, 15)
    }

    fun primaryColor(): Int {
        return Color.rgb(92, 231, 255)
    }

    fun textPrimaryColor(): Int {
        return Color.rgb(232, 247, 255)
    }

    fun textSecondaryColor(): Int {
        return Color.rgb(143, 166, 184)
    }

    fun onlineColor(): Int {
        return Color.rgb(85, 230, 165)
    }
}
