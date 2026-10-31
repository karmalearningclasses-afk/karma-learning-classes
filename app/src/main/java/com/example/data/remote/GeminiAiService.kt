package com.example.data.remote

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiAiService {
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun explainMcq(
        question: String,
        options: List<String>,
        correctOptionIndex: Int,
        userSelectedOptionIndex: Int?,
        chemicalFormula: String?,
        subject: String,
        targetLanguage: String // "Hindi" or "English"
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured in AI Studio Secrets panel. (Using verified teacher explanation).")
            )
        }

        val prompt = buildString {
            appendLine("You are an expert master chemistry teacher and tutor at Karma Learning Classes.")
            appendLine("Subject: $subject")
            if (!chemicalFormula.isNullOrBlank()) {
                appendLine("Chemical Equation/Formula: $chemicalFormula")
            }
            appendLine("Question: $question")
            appendLine("Options:")
            options.forEachIndexed { idx, opt ->
                val letter = ('A' + idx)
                appendLine("  $letter) $opt")
            }
            appendLine("Verified Teacher Correct Answer: Option ${('A' + correctOptionIndex)}: ${options.getOrNull(correctOptionIndex)}")
            if (userSelectedOptionIndex != null && userSelectedOptionIndex in options.indices) {
                appendLine("Student Selected: Option ${('A' + userSelectedOptionIndex)}: ${options[userSelectedOptionIndex]}")
            }
            appendLine("Language requested: $targetLanguage")
            appendLine()
            appendLine("Instructions:")
            appendLine("1. Respect and explain why the verified correct answer is true. Never contradict it.")
            appendLine("2. If in Hindi, explain using clear, encouraging Hinglish/Hindi educational terminology.")
            appendLine("3. Provide step-by-step chemical reasoning, mechanism, reaction steps, or formula application.")
            appendLine("4. Briefly clarify why the other options are incorrect.")
            appendLine("5. Conclude with a helpful 'Pro Tip / Master Concept' for competitive exams.")
        }

        callGeminiRest(apiKey, prompt)
    }

    suspend fun generatePracticeQuestions(
        topic: String,
        targetClass: String,
        difficulty: String,
        language: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured in AI Studio Secrets panel.")
            )
        }

        val prompt = """
            You are a senior question setter at Karma Learning Classes for $targetClass.
            Generate 2 high-yield Multiple Choice Questions on the topic '$topic' with difficulty $difficulty.
            Language: $language.
            For each question provide:
            - Question Text
            - 4 Options (A, B, C, D)
            - Correct Answer Index
            - Detailed step-by-step Chemical Mechanism or Mathematical Derivation
            - Relevant Reaction Formula / Equation
        """.trimIndent()

        callGeminiRest(apiKey, prompt)
    }

    private fun callGeminiRest(apiKey: String, prompt: String): Result<String> {
        return try {
            val url = "$BASE_URL/$MODEL_NAME:generateContent?key=$apiKey"

            val jsonBody = JSONObject().apply {
                val contentsArr = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArr = JSONArray().apply {
                            val partObj = JSONObject().apply {
                                put("text", prompt)
                            }
                            put(partObj)
                        }
                        put("parts", partsArr)
                    }
                    put(contentObj)
                }
                put("contents", contentsArr)
            }

            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return Result.failure(Exception("Gemini API error (${response.code}): $responseBody"))
            }

            val respJson = JSONObject(responseBody)
            val candidates = respJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text")

            if (!text.isNullOrBlank()) {
                Result.success(text)
            } else {
                Result.failure(Exception("Empty response from AI model."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
