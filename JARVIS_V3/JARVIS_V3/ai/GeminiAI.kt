package com.jarvis.v3.ai

import com.jarvis.v1.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class GeminiAI {

    private val apiKey = BuildConfig.GEMINI_API_KEY

    private val model = "gemini-3.8-flash"

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
                Exception("Empty message.")
            )
        }

        try {

            val url = URL(
                "https://generativelanguage.googleapis.com/" +
                    "v1beta/models/" +
                    "$model:generateContent"
            )

            val connection =
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

            connection.connectTimeout = 15000
            connection.readTimeout = 30000
            connection.doOutput = true

            val prompt = """
                You are JARVIS, a personal AI assistant.

                Respond naturally and helpfully.
                Keep normal answers concise.
                Do not claim to perform actions that you
                cannot actually perform.

                User:
                $userMessage
            """.trimIndent()

            val textPart = JSONObject()
                .put("text", prompt)

            val parts = JSONArray()
                .put(textPart)

            val content = JSONObject()
                .put("parts", parts)

            val contents = JSONArray()
                .put(content)

            val requestBody = JSONObject()
                .put("contents", contents)

            connection.outputStream.use { output ->
                output.write(
                    requestBody
                        .toString()
                        .toByteArray(Charsets.UTF_8)
                )
            }

            val responseCode = connection.responseCode

            val responseText =
                if (responseCode in 200..299) {

                    connection.inputStream
                        .bufferedReader()
                        .use { it.readText() }

                } else {

                    connection.errorStream
                        ?.bufferedReader()
                        ?.use { it.readText() }
                        ?: ""
                }

            connection.disconnect()

            if (responseCode !in 200..299) {
                return@withContext Result.failure(
                    Exception(
                        "Gemini HTTP $responseCode"
                    )
                )
            }

            val json = JSONObject(responseText)

            val candidates =
                json.optJSONArray("candidates")

            if (
                candidates == null ||
                candidates.length() == 0
            ) {
                return@withContext Result.failure(
                    Exception(
                        "Gemini returned no candidates."
                    )
                )
            }

            val candidate =
                candidates.getJSONObject(0)

            val responseContent =
                candidate.optJSONObject("content")

            val responseParts =
                responseContent?.optJSONArray("parts")

            if (
                responseParts == null ||
                responseParts.length() == 0
            ) {
                return@withContext Result.failure(
                    Exception(
                        "Gemini returned no text."
                    )
                )
            }

            val response =
                responseParts
                    .getJSONObject(0)
                    .optString("text")
                    .trim()

            if (response.isEmpty()) {
                return@withContext Result.failure(
                    Exception(
                        "Gemini returned an empty response."
                    )
                )
            }

            Result.success(response)

        } catch (e: Exception) {

            Result.failure(
                Exception(
                    "Gemini connection error: ${e.message}"
                )
            )
        }
    }
}
