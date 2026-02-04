package com.voicevault.recorder.domain.transcription

import android.content.Context
import com.voicevault.recorder.data.repository.RecordingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

// manages transcription operations across the app
class TranscriptionManager(
    private val context: Context,
    private val repository: RecordingRepository
) {

    private val voskTranscriber = VoskTranscriber(context)

    // transcribes a recording and saves to database
    suspend fun transcribeRecording(recordingId: Long, language: String = "en"): String? = withContext(Dispatchers.IO) {
        try {
            val recording = repository.getRecordingById(recordingId) ?: return@withContext null

            // check if already transcribed
            if (!recording.transcription.isNullOrEmpty()) {
                return@withContext recording.transcription
            }

            val audioFile = File(recording.filePath)
            if (!audioFile.exists()) {
                return@withContext null
            }

            // load model and transcribe
            voskTranscriber.loadModel(language)
            val transcription = voskTranscriber.transcribeFile(audioFile)

            if (transcription.isNotEmpty()) {
                repository.updateTranscription(recordingId, transcription)
            }

            transcription
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // transcribes audio file without saving to database
    suspend fun transcribeFile(file: File, language: String = "en"): String? = withContext(Dispatchers.IO) {
        try {
            voskTranscriber.loadModel(language)
            voskTranscriber.transcribeFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun release() {
        voskTranscriber.release()
    }
}