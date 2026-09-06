package com.jarvis.v1.ai

import com.jarvis.v1.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class GeminiAI {

    companion object {

        private const val PRIMARY_MODEL = "gemini-3.8-flash"

        // Backup model
        private const val FALLBACK_MODEL = "gemini-2.5-flash"

        private const val BASE_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/"
    }

    private val scope = CoroutineScope(Dispatchers.IO)

    fun ask(
        question: String,
        callback: (String) -> Unit
    ) {

        scope.launch {

            val answer = try {

                requestWithRetry(
                    PRIMARY_MODEL,
                    question
                )

            } catch (primaryError: Exception) {

                try {

                    requestWithRetry(
                        FALLBACK_MODEL,
                        question
                    )

                } catch (fallbackError: Exception) {

                    "Gemini temporarily unavailable. Please try again in a moment."
                }
            }

            withContext(Dispatchers.Main) {
                callback(answer)
            }
        }
    }

    private suspend fun requestWithRetry(
        model: String,
        question: String
    ): String {

        var lastError: Exception? = null

        repeat(3) { attempt ->

            try {

                return requestGemini(
                    model,
                    question
                )

            } catch (e: Exception) {

                lastError = e

                // Retry mainly for temporary server errors
                if (e.message?.contains("HTTP 503") == true ||
                    e.message?.contains("HTTP 429") == true
                ) {

                    delay(
                        1500L * (attempt + 1)
                    )

                } else {

                    throw e
                }
            }
        }

        throw lastError ?: Exception(
            "Gemini request failed"
        )
    }

    private fun requestGemini(
        model: String,
        question: String
    ): String {

        val apiKey = BuildConfig.GEMINI_API_KEY

        if (apiKey.isBlank()) {
            throw Exception(
                "Gemini API key is empty"
            )
        }

        val url = URL(
            "$BASE_URL$model:generateContent"
        )

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

        connection.setRequestProperty(
            "x-goog-api-key",
            apiKey
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

                                You understand Hindi, Hinglish and English.

                                Reply in the same language used by the user.

                                Be helpful, friendly and concise.

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

                        put(
                            "role",
                            "user"
                        )

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

                    put(
                        "temperature",
                        0.7
                    )

                    put(
                        "maxOutputTokens",
                        512
                    )
                }
            )
        }

        connection.outputStream.use { output ->

            output.write(
                body.toString()
                    .toByteArray(Charsets.UTF_8)
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
            stream.bufferedReader()
                .use { it.readText() }

        connection.disconnect()

        if (responseCode !in 200..299) {

            throw Exception(
                "Gemini API error: HTTP $responseCode - $responseText"
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
