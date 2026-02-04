package com.voicevault.recorder.domain.llm

import com.voicevault.recorder.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

// euron api client implementation
class EuronClient(private val apiKey: String, private val model: String) : LLMProvider {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    override suspend fun sendMessage(
        message: String,
        conversationHistory: List<Pair<String, String>>
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val messages = JSONArray()

            conversationHistory.forEach { (role, content) ->
                messages.put(JSONObject().apply {
                    put("role", role)
                    put("content", content)
                })
            }

            messages.put(JSONObject().apply {
                put("role", "user")
                put("content", message)
            })

            val requestBody = JSONObject().apply {
                put("model", model)
                put("messages", messages)
                put("temperature", 0.7)
                put("max_tokens", 2000)
            }

            val request = Request.Builder()
                .url("${Constants.EURON_BASE_URL}chat/completions")
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful && responseBody != null) {
                val jsonResponse = JSONObject(responseBody)
                val content = jsonResponse
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")

                Result.success(content)
            } else {
                Result.failure(Exception("Euron API error: ${response.code} - $responseBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun testConnection(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val result = sendMessage("test")
            if (result.isSuccess) {
                Result.success(true)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Connection test failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAvailableModels(): List<String> = Constants.EURON_MODELS
}