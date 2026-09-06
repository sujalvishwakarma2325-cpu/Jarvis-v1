package com.jarvis.v1.phone

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

class PhoneControl(
    private val context: Context
) {

    fun openAppByName(
        appName: String,
        callback: (String) -> Unit
    ) {
        val packageManager = context.packageManager

        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val apps = packageManager.queryIntentActivities(intent, 0)

        val match = apps.firstOrNull { info ->
            val label = info.loadLabel(packageManager)
                .toString()
                .lowercase()

            label == appName.lowercase() ||
                    label.contains(appName.lowercase())
        }

        if (match != null) {
            val launchIntent =
                packageManager.getLaunchIntentForPackage(
                    match.activityInfo.packageName
                )

            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)

                callback("Opening $appName.")
                return
            }
        }

        callback("I could not find $appName on this phone.")
    }

    fun openSettings(
        callback: (String) -> Unit
    ) {
        try {
            val intent = Intent(Settings.ACTION_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)

            callback("Opening settings.")
        } catch (e: Exception) {
            callback("I could not open settings.")
        }
    }

    fun openPhone(
        callback: (String) -> Unit
    ) {
        try {
            val intent = Intent(
                Intent.ACTION_DIAL,
                Uri.parse("tel:")
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)

            callback("Opening phone.")
        } catch (e: Exception) {
            callback("I could not open the phone app.")
        }
    }

    fun openCalculator(
        callback: (String) -> Unit
    ) {
        openAppByName("Calculator", callback)
    }
}
