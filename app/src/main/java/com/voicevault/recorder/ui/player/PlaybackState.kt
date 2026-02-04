package com.voicevault.recorder.ui.player

// represents the current playback state
data class PlaybackState(
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0,
    val duration: Long = 0,
    val speed: Float = 1.0f,
    val isRepeat: Boolean = false,
    val waveformData: List<Float> = emptyList(),
    val bookmarks: List<Long> = emptyList()
)