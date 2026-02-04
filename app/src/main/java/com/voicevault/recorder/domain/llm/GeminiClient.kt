package com.voicevault.recorder.domain.llm

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.voicevault.recorder.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// gemini api client using google's official sdk
class GeminiClient(private val apiKey: String, private val modelName: String) : LLMProvider {

    private val generativeModel = GenerativeModel(
        modelName = modelName,
        apiKey = apiKey
    )

    override suspend fun sendMessage(
        message: String,
        conversationHistory: List<Pair<String, String>>
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val chat = generativeModel.startChat(
                history = conversationHistory.map { (role, text) ->
                    content(role) { text(text) }
                }
            )

            val response = chat.sendMessage(message)
            Result.success(response.text ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun testConnection(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = generativeModel.generateContent("test")
            if (response.text != null) {
                Result.success(true)
            } else {
                Result.failure(Exception("No response from Gemini"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAvailableModels(): List<String> = Constants.GEMINI_MODELS
}