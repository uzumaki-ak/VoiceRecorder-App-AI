package com.voicevault.recorder.domain.recorder

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import com.voicevault.recorder.utils.Constants
import com.voicevault.recorder.utils.FileUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

// handles audio recording using mediarecorder
class AudioRecorder(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var recordingFile: File? = null

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused

    private val _recordingDuration = MutableStateFlow(0L)
    val recordingDuration: StateFlow<Long> = _recordingDuration

    private var startTime = 0L
    private var pausedDuration = 0L

    // starts a new recording with specified quality
    fun startRecording(
        fileName: String,
        quality: Constants.RecordingQuality,
        useSDCard: Boolean = false
    ): File? {
        try {
            // create file for recording
            recordingFile = FileUtils.createRecordingFile(context, fileName, useSDCard)

            // create media recorder based on android version
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(quality.bitrate)
                setAudioSamplingRate(quality.sampleRate)
                setOutputFile(recordingFile?.absolutePath)

                prepare()
                start()

                _isRecording.value = true
                _isPaused.value = false
                startTime = System.currentTimeMillis()
            }

            return recordingFile
        } catch (e: Exception) {
            e.printStackTrace()
            releaseRecorder()
            return null
        }
    }

    // pauses current recording
    fun pauseRecording() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mediaRecorder?.pause()
                _isPaused.value = true
                pausedDuration += System.currentTimeMillis() - startTime
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // resumes paused recording
    fun resumeRecording() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mediaRecorder?.resume()
                _isPaused.value = false
                startTime = System.currentTimeMillis()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // stops and finalizes recording
    fun stopRecording(): File? {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            _isRecording.value = false
            _isPaused.value = false
            _recordingDuration.value = 0L

            return recordingFile
        } catch (e: Exception) {
            e.printStackTrace()
            releaseRecorder()
            return null
        }
    }

    // cancels recording and deletes file
    fun cancelRecording() {
        releaseRecorder()
        recordingFile?.delete()
        recordingFile = null
    }

    // gets current amplitude for waveform visualization
    fun getMaxAmplitude(): Int {
        return try {
            mediaRecorder?.maxAmplitude ?: 0
        } catch (e: Exception) {
            0
        }
    }

    // updates recording duration
    fun updateDuration() {
        if (_isRecording.value && !_isPaused.value) {
            _recordingDuration.value = pausedDuration + (System.currentTimeMillis() - startTime)
        }
    }

    private fun releaseRecorder() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaRecorder = null
            _isRecording.value = false
            _isPaused.value = false
            _recordingDuration.value = 0L
        }
    }
}