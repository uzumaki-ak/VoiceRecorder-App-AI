package com.voicevault.recorder.domain.llm

import com.voicevault.recorder.data.preferences.AppPreferences
import com.voicevault.recorder.utils.Constants
import kotlinx.coroutines.flow.first

// manages llm provider selection and api calls
class LLMManager(private val preferences: AppPreferences) {

    private var currentProvider: LLMProvider? = null

    // initializes the current llm provider based on settings
    suspend fun initializeProvider(): Result<Boolean> {
        return try {
            val provider = preferences.llmProvider.first()
            val model = preferences.llmModel.first()
            val apiKey = preferences.getApiKey(provider).first()

            if (apiKey.isEmpty()) {
                return Result.failure(Exception("No API key configured for $provider"))
            }

            currentProvider = when (provider) {
                Constants.PROVIDER_GROQ -> GroqClient(apiKey, model)
                Constants.PROVIDER_GEMINI -> GeminiClient(apiKey, model)
                Constants.PROVIDER_EURON -> EuronClient(apiKey, model)
                Constants.PROVIDER_OPENROUTER -> OpenRouterClient(apiKey, model)
                Constants.PROVIDER_MISTRAL -> MistralClient(apiKey, model)
                else -> return Result.failure(Exception("Unknown provider: $provider"))
            }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // sends message to current provider
    suspend fun sendMessage(
        message: String,
        conversationHistory: List<Pair<String, String>> = emptyList()
    ): Result<String> {
        if (currentProvider == null) {
            initializeProvider()
        }

        return currentProvider?.sendMessage(message, conversationHistory)
            ?: Result.failure(Exception("No provider initialized"))
    }

    // tests connection with current provider
    suspend fun testConnection(): Result<Boolean> {
        return currentProvider?.testConnection()
            ?: Result.failure(Exception("No provider initialized"))
    }

    // gets available models for current provider
    fun getAvailableModels(): List<String> {
        return currentProvider?.getAvailableModels() ?: emptyList()
    }
}