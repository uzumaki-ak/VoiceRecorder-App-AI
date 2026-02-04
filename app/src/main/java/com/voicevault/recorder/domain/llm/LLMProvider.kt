package com.voicevault.recorder.domain.llm

// interface that all llm providers must implement
interface LLMProvider {

    // sends a message and gets response
    suspend fun sendMessage(
        message: String,
        conversationHistory: List<Pair<String, String>> = emptyList()
    ): Result<String>

    // tests if the api connection works
    suspend fun testConnection(): Result<Boolean>

    // gets list of available models for this provider
    fun getAvailableModels(): List<String>
}

// sealed class for llm results
sealed class LLMResult {
    data class Success(val text: String) : LLMResult()
    data class Error(val message: String) : LLMResult()
}