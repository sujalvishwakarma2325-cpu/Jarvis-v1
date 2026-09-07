package com.jarvis.v1

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.jarvis.v3.ui.JarvisUI

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val screen = JarvisUI.createMainScreen(
            context = this,
            onTalkClick = {
                // Voice module will be connected here.
            }
        )

        setContentView(screen)
    }
}
