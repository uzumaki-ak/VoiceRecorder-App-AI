package com.voicevault.recorder.ui.home

// represents the current state of recording
sealed class RecordingState {
    object Idle : RecordingState()
    data class Recording(
        val duration: Long,
        val isPaused: Boolean,
        val waveformData: List<Float>
    ) : RecordingState()
}