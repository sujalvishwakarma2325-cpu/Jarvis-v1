package com.jarvis.v3.ai

import com.jarvis.v1.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class GeminiAI {

    private val apiKey = BuildConfig.GEMINI_API_KEY

    // Current Flash model
    private val model = "gemini-3.8-flash"

    private val maxRetries = 4

    suspend fun askJarvis(
        userMessage: String
    ): Result<String> = withContext(Dispatchers.IO) {

        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                Exception("Gemini API key is missing.")
            )
        }

        if (userMessage.isBlank()) {
            return@withContext Result.failure(
                Exception("I didn't receive your question.")
            )
        }

        var lastError = "Unknown Gemini error"

        for (attempt in 0..maxRetries) {

            try {

                val result = makeRequest(userMessage)

                if (result.isSuccess) {
                    return@withContext result
                }

                val errorMessage =
                    result.exceptionOrNull()?.message
                        ?: "Unknown Gemini error"

                lastError = errorMessage

                // Retry only temporary server errors.
                if (
                    !errorMessage.startsWith("Gemini HTTP 503") &&
                    !errorMessage.startsWith("Gemini HTTP 429") &&
                    !errorMessage.startsWith("Gemini HTTP 500") &&
                    !errorMessage.startsWith("Gemini HTTP 502")
                ) {
                    return@withContext result
                }

                if (attempt < maxRetries) {

                    val delayTime =
                        when (attempt) {
                            0 -> 2000L
                            1 -> 4000L
                            2 -> 8000L
                            else -> 16000L
                        }

                    delay(delayTime)
                }

            } catch (e: Exception) {

                lastError =
                    e.message ?: "Network error"

                if (attempt < maxRetries) {

                    val delayTime =
                        when (attempt) {
                            0 -> 2000L
                            1 -> 4000L
                            2 -> 8000L
                            else -> 16000L
                        }

                    delay(delayTime)

                } else {
                    break
                }
            }
        }

        Result.failure(
            Exception(
                "$lastError\n\nGemini server is temporarily unavailable. Please try again."
            )
        )
    }

    private fun makeRequest(
        userMessage: String
    ): Result<String> {

        var connection: HttpURLConnection? = null

        try {

            val url = URL(
                "https://generativelanguage.googleapis.com/" +
                        "v1beta/models/$model:generateContent"
            )

            connection =
                url.openConnection() as HttpURLConnection

            connection.requestMethod = "POST"

            connection.setRequestProperty(
                "Content-Type",
                "application/json"
            )

            connection.setRequestProperty(
                "x-goog-api-key",
                apiKey
            )

            connection.connectTimeout = 20000
            connection.readTimeout = 60000

            connection.doOutput = true

            val requestBody =
                JSONObject().apply {

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
                                            """
                                            You are JARVIS, a personal AI assistant.

                                            Respond naturally and helpfully.
                                            Keep normal voice-assistant answers concise.
                                            The user may speak in English, Hindi, or Hinglish.

                                            User:
                                            $userMessage
                                            """.trimIndent()
                                        )
                                    )
                                )
                            }
                        )
                    )
                }

            connection.outputStream.use { output ->
                output.write(
                    requestBody.toString()
                        .toByteArray(Charsets.UTF_8)
                )
            }

            val responseCode =
                connection.responseCode

            val inputStream =
                if (responseCode in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

            val responseText =
                if (inputStream != null) {

                    BufferedReader(
                        InputStreamReader(
                            inputStream,
                            Charsets.UTF_8
                        )
                    ).use { reader ->
                        reader.readText()
                    }

                } else {
                    ""
                }

            if (responseCode !in 200..299) {

                var serverMessage = responseText

                try {

                    val errorJson =
                        JSONObject(responseText)

                    val error =
                        errorJson.optJSONObject("error")

                    if (error != null) {

                        serverMessage =
                            error.optString(
                                "message",
                                responseText
                            )
                    }

                } catch (_: Exception) {
                    // Keep raw response.
                }

                return Result.failure(
                    Exception(
                        "Gemini HTTP $responseCode: $serverMessage"
                    )
                )
            }

            val json =
                JSONObject(responseText)

            val candidates =
                json.optJSONArray("candidates")

            if (
                candidates == null ||
                candidates.length() == 0
            ) {

                return Result.failure(
                    Exception(
                        "Gemini returned no response."
                    )
                )
            }

            val firstCandidate =
                candidates.optJSONObject(0)

            val content =
                firstCandidate?.optJSONObject("content")

            val parts =
                content?.optJSONArray("parts")

            if (
                parts == null ||
                parts.length() == 0
            ) {

                return Result.failure(
                    Exception(
                        "Gemini returned an empty response."
                    )
                )
            }

            val answer =
                parts
                    .optJSONObject(0)
                    ?.optString("text", "")
                    ?.trim()
                    ?: ""

            if (answer.isBlank()) {

                return Result.failure(
                    Exception(
                        "Gemini returned an empty answer."
                    )
                )
            }

            return Result.success(answer)

        } catch (e: Exception) {

            return Result.failure(
                Exception(
                    "Network error: ${
                        e.message ?: "Unknown error"
                    }"
                )
            )

        } finally {

            connection?.disconnect()
        }
    }
}
