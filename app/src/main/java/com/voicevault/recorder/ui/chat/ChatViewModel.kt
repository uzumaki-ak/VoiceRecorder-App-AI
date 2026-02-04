package com.voicevault.recorder.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.voicevault.recorder.VoiceVaultApp
import com.voicevault.recorder.data.preferences.AppPreferences
import com.voicevault.recorder.data.repository.RecordingRepository
import com.voicevault.recorder.domain.llm.LLMManager
import com.voicevault.recorder.domain.transcription.TranscriptionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

// viewmodel for chat screen - manages conversation with llm about recording
class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val recordingRepository: RecordingRepository
    private val preferences: AppPreferences
    private val llmManager: LLMManager
    private val transcriptionManager: TranscriptionManager

    init {
        val database = (application as VoiceVaultApp).database
        recordingRepository = RecordingRepository(database.recordingDao())
        preferences = AppPreferences(application)
        llmManager = LLMManager(preferences)
        transcriptionManager = TranscriptionManager(application, recordingRepository)
    }

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _transcription = MutableStateFlow<String?>(null)
    val transcription: StateFlow<String?> = _transcription.asStateFlow()

    private var conversationHistory = mutableListOf<Pair<String, String>>()

    // loads recording and transcribes it
    fun loadRecording(recordingId: Long) {
        viewModelScope.launch {
            val recording = recordingRepository.getRecordingById(recordingId)
            recording?.let {
                // check if already transcribed
                if (!it.transcription.isNullOrEmpty()) {
                    _transcription.value = it.transcription
                    initializeConversation(it.transcription!!)
                } else {
                    // transcribe first
                    _isLoading.value = true
                    val transcribedText = transcriptionManager.transcribeRecording(recordingId)
                    _isLoading.value = false

                    transcribedText?.let { text ->
                        _transcription.value = text
                        initializeConversation(text)
                    }
                }
            }
        }
    }

    // initializes conversation with system prompt including transcription
    private fun initializeConversation(transcription: String) {
        viewModelScope.launch {
            llmManager.initializeProvider()

            // FIXED: Using roles that are universally supported (user/assistant)
            val systemContext = """You are a helpful assistant analyzing an audio recording transcription. 
                    |Analyze the following transcription:
                    |$transcription
                """.trimMargin()
            
            conversationHistory.add("user" to systemContext)
            conversationHistory.add("assistant" to "I have analyzed the recording transcription. I'm ready to answer any questions you have about it.")

            // add initial greeting
            val greeting = ChatMessage(
                id = UUID.randomUUID().toString(),
                content = "I've analyzed your recording. What would you like to know about it?",
                isUser = false,
                timestamp = System.currentTimeMillis()
            )
            _messages.value = listOf(greeting)
        }
    }

    // sends user message and gets llm response
    fun sendMessage(message: String) {
        viewModelScope.launch {
            // add user message
            val userMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                content = message,
                isUser = true,
                timestamp = System.currentTimeMillis()
            )
            _messages.value = _messages.value + userMessage

            // show loading
            _isLoading.value = true

            // get llm response
            val result = llmManager.sendMessage(message, conversationHistory)

            _isLoading.value = false

            result.fold(
                onSuccess = { response ->
                    val assistantMessage = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        content = response,
                        isUser = false,
                        timestamp = System.currentTimeMillis()
                    )
                    _messages.value = _messages.value + assistantMessage

                    // update conversation history
                    conversationHistory.add("user" to message)
                    conversationHistory.add("assistant" to response)
                },
                onFailure = { error ->
                    val errorMessage = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        content = "Error: ${error.message}",
                        isUser = false,
                        timestamp = System.currentTimeMillis()
                    )
                    _messages.value = _messages.value + errorMessage
                }
            )
        }
    }

    // clears conversation
    fun clearConversation() {
        _messages.value = emptyList()
        conversationHistory.clear()
        _transcription.value?.let { initializeConversation(it) }
    }
}