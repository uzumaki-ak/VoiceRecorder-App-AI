package com.voicevault.recorder.ui.recyclebin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.voicevault.recorder.VoiceVaultApp
import com.voicevault.recorder.data.database.entities.Recording
import com.voicevault.recorder.data.repository.RecordingRepository
import com.voicevault.recorder.utils.FileUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// viewmodel for recycle bin - NEW
class RecycleBinViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RecordingRepository

    init {
        val database = (application as VoiceVaultApp).database
        repository = RecordingRepository(database.recordingDao())
    }

    val deletedRecordings: StateFlow<List<Recording>> = repository.getDeletedRecordings()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    fun restoreRecording(recordingId: Long) {
        viewModelScope.launch {
            repository.restoreFromRecycleBin(recordingId)
        }
    }

    fun permanentlyDelete(recordingId: Long) {
        viewModelScope.launch {
            val recording = repository.getRecordingById(recordingId)
            recording?.let {
                FileUtils.deleteFile(it.filePath)
                repository.deleteRecording(it)
            }
        }
    }

    fun emptyRecycleBin() {
        viewModelScope.launch {
            deletedRecordings.value.forEach { recording ->
                FileUtils.deleteFile(recording.filePath)
                repository.deleteRecording(recording)
            }
        }
    }
}