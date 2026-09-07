package com.jarvis.v3.ai

import com.jarvis.v1.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class GeminiAI {

    private val apiKey =
        BuildConfig.GEMINI_API_KEY

    private val models =
        listOf(
            "gemini-2.5-flash"
        )

    suspend fun askJarvis(
        userMessage: String
    ): Result<String> = withContext(Dispatchers.IO) {

        if (apiKey.isBlank()) {

            return@withContext Result.failure(
                Exception(
                    "Gemini API key is missing."
                )
            )
        }

        if (userMessage.isBlank()) {

            return@withContext Result.failure(
                Exception(
                    "Empty message."
                )
            )
        }

        var lastError: Exception? = null

        for (model in models) {

            for (attempt in 0 until 3) {

                try {

                    val url = URL(
                        "https://generativelanguage.googleapis.com/" +
                            "v1beta/models/" +
                            "$model:generateContent"
                    )

                    val connection =
                        url.openConnection()
                            as HttpURLConnection

                    connection.requestMethod = "POST"

                    connection.setRequestProperty(
                        "Content-Type",
                        "application/json"
                    )

                    connection.setRequestProperty(
                        "x-goog-api-key",
                        apiKey
                    )

                    connection.connectTimeout =
                        15000

                    connection.readTimeout =
                        30000

                    connection.doOutput = true

                    val systemPrompt = """
                        You are JARVIS, a personal AI assistant.

                        Speak naturally and helpfully.

                        Keep answers concise unless
                        the user asks for detail.

                        Do not claim to perform actions
                        that you cannot actually perform.

                        User message:
                        $userMessage
                    """.trimIndent()

                    val part =
                        JSONObject()
                            .put(
                                "text",
                                systemPrompt
                            )

                    val parts =
                        JSONArray()
                            .put(part)

                    val content =
                        JSONObject()
                            .put(
                                "parts",
                                parts
                            )

                    val contents =
                        JSONArray()
                            .put(content)

                    val requestBody =
                        JSONObject()
                            .put(
                                "contents",
                                contents
                            )

                    connection.outputStream.use { output ->

                        output.write(
                            requestBody
                                .toString()
                                .toByteArray(
                                    Charsets.UTF_8
                                )
                        )
                    }

                    val responseCode =
                        connection.responseCode

                    val responseText =
                        if (
                            responseCode in 200..299
                        ) {

                            connection.inputStream
                                .bufferedReader()
                                .use {
                                    it.readText()
                                }

                        } else {

                            connection.errorStream
                                ?.bufferedReader()
                                ?.use {
                                    it.readText()
                                }
                                ?: ""
                        }

                    connection.disconnect()

                    if (
                        responseCode in 200..299
                    ) {

                        val json =
                            JSONObject(
                                responseText
                            )

                        val candidates =
                            json.optJSONArray(
                                "candidates"
                            )

                        if (
                            candidates != null &&
                            candidates.length() > 0
                        ) {

                            val candidate =
                                candidates
                                    .getJSONObject(0)

                            val responseContent =
                                candidate
                                    .optJSONObject(
                                        "content"
                                    )

                            val responseParts =
                                responseContent
                                    ?.optJSONArray(
                                        "parts"
                                    )

                            if (
                                responseParts != null &&
                                responseParts.length() > 0
                            ) {

                                val text =
                                    responseParts
                                        .getJSONObject(0)
                                        .optString("text")
                                        .trim()

                                if (
                                    text.isNotEmpty()
                                ) {

                                    return@withContext Result.success(
                                        text
                                    )
                                }
                            }
                        }

                        lastError =
                            Exception(
                                "Gemini returned an empty response."
                            )

                    } else {

                        lastError =
                            Exception(
                                "Gemini HTTP " +
                                    "$responseCode: " +
                                    responseText
                            )

                        if (
                            responseCode != 429 &&
                            responseCode != 503
                        ) {
                            break
                        }
                    }

                } catch (e: Exception) {

                    lastError = e
                }

                if (attempt < 2) {

                    Thread.sleep(
                        1000L * (attempt + 1)
                    )
                }
            }
        }

        Result.failure(
            lastError
                ?: Exception(
                    "Gemini request failed."
                )
        )
    }
}
