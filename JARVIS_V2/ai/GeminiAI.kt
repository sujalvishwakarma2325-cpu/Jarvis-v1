package com.jarvis.v1.ai

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class GeminiAI {

    companion object {
        private const val MODEL = "gemini-2.5-flash"

        // API key build ke time GitHub Actions se inject hogi.
        private const val API_KEY = BuildConfig.GEMINI_API_KEY

        private const val API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"
    }

    private val scope =
        CoroutineScope(Dispatchers.IO)

    fun ask(
        question: String,
        callback: (String) -> Unit
    ) {

        scope.launch {

            val answer = try {
                requestGemini(question)
            } catch (e: Exception) {
                "Sorry, I could not connect to Gemini right now."
            }

            withContext(Dispatchers.Main) {
                callback(answer)
            }
        }
    }

    private fun requestGemini(
        question: String
    ): String {

        val url =
            URL("$API_URL?key=$API_KEY")

        val connection =
            url.openConnection() as HttpURLConnection

        connection.requestMethod = "POST"
        connection.connectTimeout = 15000
        connection.readTimeout = 30000
        connection.doOutput = true

        connection.setRequestProperty(
            "Content-Type",
            "application/json"
        )

        val body = JSONObject().apply {

            put(
                "systemInstruction",
                JSONObject().apply {
                    put(
                        "parts",
                        JSONArray().put(
                            JSONObject().put(
                                "text",
                                """
                                You are JARVIS, a helpful personal AI assistant.

                                Reply naturally and briefly.
                                You can understand Hindi, Hinglish and English.
                                Reply in the same language the user uses.

                                Do not mention that you are a language model.
                                """.trimIndent()
                            )
                        )
                    )
                }
            )

            put(
                "contents",
                JSONArray().put(
                    JSONObject().apply {
                        put("role", "user")

                        put(
                            "parts",
                            JSONArray().put(
                                JSONObject().put(
                                    "text",
                                    question
                                )
                            )
                        )
                    }
                )
            )

            put(
                "generationConfig",
                JSONObject().apply {
                    put("temperature", 0.7)
                    put("maxOutputTokens", 512)
                }
            )
        }

        connection.outputStream.use { output ->
            output.write(
                body.toString().toByteArray(
                    Charsets.UTF_8
                )
            )
        }

        val responseCode =
            connection.responseCode

        val stream =
            if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

        val responseText =
            stream.bufferedReader().use {
                it.readText()
            }

        connection.disconnect()

        if (responseCode !in 200..299) {
            throw Exception(
                "Gemini API error: $responseCode"
            )
        }

        val json =
            JSONObject(responseText)

        return json
            .getJSONArray("candidates")
            .getJSONObject(0)
            .getJSONObject("content")
            .getJSONArray("parts")
            .getJSONObject(0)
            .getString("text")
            .trim()
    }
}
