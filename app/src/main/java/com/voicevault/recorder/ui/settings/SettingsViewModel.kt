package com.voicevault.recorder.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.voicevault.recorder.data.preferences.AppPreferences
import com.voicevault.recorder.utils.Constants
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// viewmodel for settings screen - manages app preferences
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = AppPreferences(application)

    val recordingQuality: StateFlow<Constants.RecordingQuality> = preferences.recordingQuality
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            Constants.RecordingQuality.MEDIUM
        )

    val blockCalls: StateFlow<Boolean> = preferences.blockCalls
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val autoPlayNext: StateFlow<Boolean> = preferences.autoPlayNext
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val useBluetooth: StateFlow<Boolean> = preferences.useBluetooth
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val storageLocation: StateFlow<String> = preferences.storageLocation
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Constants.STORAGE_INTERNAL)

    val recycleBinEnabled: StateFlow<Boolean> = preferences.recycleBinEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val saveSearches: StateFlow<Boolean> = preferences.saveSearches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val autoTranscribe: StateFlow<Boolean> = preferences.autoTranscribe
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setRecordingQuality(quality: Constants.RecordingQuality) {
        viewModelScope.launch {
            preferences.setRecordingQuality(quality)
        }
    }

    fun setBlockCalls(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setBlockCalls(enabled)
        }
    }

    fun setAutoPlayNext(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setAutoPlayNext(enabled)
        }
    }

    fun setUseBluetooth(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setUseBluetooth(enabled)
        }
    }

    fun setStorageLocation(location: String) {
        viewModelScope.launch {
            preferences.setStorageLocation(location)
        }
    }

    fun setRecycleBinEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setRecycleBinEnabled(enabled)
        }
    }

    fun setSaveSearches(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setSaveSearches(enabled)
        }
    }

    fun setAutoTranscribe(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setAutoTranscribe(enabled)
        }
    }
}