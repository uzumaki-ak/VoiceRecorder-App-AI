package com.voicevault.recorder.domain.recorder

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import com.voicevault.recorder.utils.Constants
import com.voicevault.recorder.utils.FileUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

// handles audio recording using MediaRecorder - FIXED
class AudioRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var activeRecordingFile: File? = null

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused

    private val _recordingDuration = MutableStateFlow(0L)
    val recordingDuration: StateFlow<Long> = _recordingDuration

    // starts a new recording
    fun startRecording(fileName: String, quality: Constants.RecordingQuality, useSDCard: Boolean): File? {
        try {
            val dir = FileUtils.getRecordingsDirectory(context, useSDCard)
            activeRecordingFile = File(dir, "$fileName.${Constants.AUDIO_FORMAT}")

            recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(quality.bitrate)
                setAudioSamplingRate(quality.sampleRate)
                setOutputFile(activeRecordingFile?.absolutePath)
                prepare()
                start()
            }
            _isRecording.value = true
            return activeRecordingFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    // pauses the current recording
    fun pauseRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            recorder?.pause()
            _isPaused.value = true
        }
    }

    // resumes the paused recording
    fun resumeRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            recorder?.resume()
            _isPaused.value = false
        }
    }

    // stops the recording and returns the file
    fun stopRecording(): File? {
        return try {
            recorder?.apply {
                stop()
                release()
            }
            recorder = null
            _isRecording.value = false
            _isPaused.value = false
            activeRecordingFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // cancels the recording and deletes the file
    fun cancelRecording() {
        stopRecording()
        activeRecordingFile?.delete()
        activeRecordingFile = null
        reset()
    }

    // updates the recording duration
    fun updateDuration() {
        if (_isRecording.value && !_isPaused.value) {
            _recordingDuration.value += 100 // assuming called every 100ms
        }
    }

    // gets max amplitude for waveform
    fun getMaxAmplitude(): Int {
        return try {
            recorder?.maxAmplitude ?: 0
        } catch (e: Exception) {
            0
        }
    }

    // resets recording state
    fun reset() {
        _recordingDuration.value = 0L
        _isRecording.value = false
        _isPaused.value = false
    }
}