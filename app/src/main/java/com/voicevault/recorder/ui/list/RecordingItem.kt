package com.voicevault.recorder.ui.list

import com.voicevault.recorder.data.database.entities.Recording

// ui model for displaying recording in list
data class RecordingItem(
    val recording: Recording,
    val isSelected: Boolean = false
)