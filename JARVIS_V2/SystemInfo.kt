package com.jarvis.v1.system

import android.content.Context
import android.os.BatteryManager
import android.os.Build

class SystemInfo(
    private val context: Context
) {

    fun getBatteryInfo(): String {
        val batteryManager =
            context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

        val level = batteryManager.getIntProperty(
            BatteryManager.BATTERY_PROPERTY_CAPACITY
        )

        val charging = batteryManager.isCharging

        return if (charging) {
            "Battery is at $level percent and the phone is currently charging."
        } else {
            "Battery is at $level percent and the phone is not charging."
        }
    }

    fun getAndroidVersion(): String {
        return "This phone is running Android ${Build.VERSION.RELEASE}."
    }

    fun getDeviceInfo(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL

        return "Your device is $manufacturer $model."
    }

    fun getSystemSummary(): String {
        return """
            ${getDeviceInfo()}
            ${getAndroidVersion()}
            ${getBatteryInfo()}
        """.trimIndent()
    }
}
