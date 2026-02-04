package com.voicevault.recorder.ui.chat

// represents a single chat message
data class ChatMessage(
    val id: String,
    val content: String,
    val isUser: Boolean,
    val timestamp: Long
)