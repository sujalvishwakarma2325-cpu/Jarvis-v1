package com.jarvis.v1.ai

class GeminiAI {

    fun ask(
        question: String,
        callback: (String) -> Unit
    ) {
        // Gemini API module
        // Is module ko next step mein actual Gemini API se connect karenge.

        callback(
            "Gemini AI module is ready."
        )
    }
}
