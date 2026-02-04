package com.voicevault.recorder.ui.home

import android.app.Application
import android.media.MediaPlayer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.voicevault.recorder.VoiceVaultApp
import com.voicevault.recorder.data.database.entities.Recording
import com.voicevault.recorder.data.preferences.AppPreferences
import com.voicevault.recorder.data.repository.RecordingRepository
import com.voicevault.recorder.domain.recorder.AudioRecorder
import com.voicevault.recorder.utils.Constants
import com.voicevault.recorder.utils.FileUtils
import com.voicevault.recorder.utils.generateFileName
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

// viewmodel for home screen - manages recording state and operations - FIXED ALL FUNCTIONS
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RecordingRepository
    private val preferences: AppPreferences
    private val audioRecorder: AudioRecorder
    private var previewPlayer: MediaPlayer? = null

    init {
        val database = (application as VoiceVaultApp).database
        repository = RecordingRepository(database.recordingDao())
        preferences = AppPreferences(application)
        audioRecorder = AudioRecorder(application)
    }

    private val _recordingState = MutableStateFlow<RecordingState>(RecordingState.Idle)
    val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()

    private val _showSaveDialog = MutableStateFlow(false)
    val showSaveDialog: StateFlow<Boolean> = _showSaveDialog.asStateFlow()

    private var currentRecordingFile: File? = null
    private val waveformAmplitudes = mutableListOf<Int>()

    // starts new recording
    fun startRecording() {
        viewModelScope.launch {
            val quality = preferences.recordingQuality.first()
            val useSDCard = preferences.storageLocation.first() == Constants.STORAGE_SD_CARD

            val fileName = generateFileName()
            currentRecordingFile = audioRecorder.startRecording(fileName, quality, useSDCard)

            if (currentRecordingFile != null) {
                startRecordingTimer()
            }
        }
    }

    // pauses current recording
    fun pauseRecording() {
        audioRecorder.pauseRecording()
    }

    // resumes paused recording
    fun resumeRecording() {
        audioRecorder.resumeRecording()
    }

    // FIXED: preview function - plays recording from beginning while still recording
    fun previewRecording() {
        viewModelScope.launch {
            currentRecordingFile?.let { file ->
                try {
                    // pause recording first
                    val wasPaused = audioRecorder.isPaused.value
                    if (!wasPaused) {
                        audioRecorder.pauseRecording()
                    }

                    // play preview
                    previewPlayer?.release()
                    previewPlayer = MediaPlayer().apply {
                        setDataSource(file.absolutePath)
                        prepare()
                        start()

                        // resume recording after preview
                        setOnCompletionListener {
                            if (!wasPaused) {
                                audioRecorder.resumeRecording()
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // stops recording and shows save dialog
    fun stopRecording() {
        previewPlayer?.release()
        previewPlayer = null
        currentRecordingFile = audioRecorder.stopRecording()
        _showSaveDialog.value = true
    }

    // FIXED: properly dismiss save dialog
    fun dismissSaveDialog() {
        _showSaveDialog.value = false
        cancelRecording()
    }

    // cancels recording without saving
    fun cancelRecording() {
        audioRecorder.cancelRecording()
        currentRecordingFile?.delete()
        currentRecordingFile = null
        waveformAmplitudes.clear()
        _recordingState.value = RecordingState.Idle
    }

    // saves recording to database
    fun saveRecording(fileName: String, categoryId: Long?) {
        viewModelScope.launch {
            currentRecordingFile?.let { file ->
                val quality = preferences.recordingQuality.first()

                // rename file if needed
                val finalFile = if (fileName != file.nameWithoutExtension) {
                    val newFile = File(file.parent, "$fileName.${Constants.AUDIO_FORMAT}")
                    file.renameTo(newFile)
                    newFile
                } else {
                    file
                }

                val recording = Recording(
                    fileName = fileName,
                    filePath = finalFile.absolutePath,
                    duration = audioRecorder.recordingDuration.value,
                    fileSize = FileUtils.getFileSize(finalFile.absolutePath),
                    createdAt = System.currentTimeMillis(),
                    categoryId = categoryId,
                    bitrate = quality.bitrate,
                    sampleRate = quality.sampleRate
                )

                repository.insertRecording(recording)

                _showSaveDialog.value = false
                currentRecordingFile = null
                waveformAmplitudes.clear()
                _recordingState.value = RecordingState.Idle
            }
        }
    }

    // updates recording duration and waveform in real time
    private fun startRecordingTimer() {
        viewModelScope.launch {
            while (audioRecorder.isRecording.value) {
                audioRecorder.updateDuration()

                // collect amplitude for waveform
                val amplitude = audioRecorder.getMaxAmplitude()
                waveformAmplitudes.add(amplitude)

                // keep only last 100 samples for performance
                if (waveformAmplitudes.size > 100) {
                    waveformAmplitudes.removeAt(0)
                }

                // normalize amplitudes to 0-1 range for display
                val normalizedWaveform = waveformAmplitudes.map { it.toFloat() / 32767f }

                _recordingState.value = RecordingState.Recording(
                    duration = audioRecorder.recordingDuration.value,
                    isPaused = audioRecorder.isPaused.value,
                    waveformData = normalizedWaveform
                )

                delay(100) // update 10 times per second
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        previewPlayer?.release()
        audioRecorder.cancelRecording()
    }
}