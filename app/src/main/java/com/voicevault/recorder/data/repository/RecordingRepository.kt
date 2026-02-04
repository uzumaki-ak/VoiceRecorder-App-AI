package com.voicevault.recorder.data.repository

import com.voicevault.recorder.data.database.RecordingDao
import com.voicevault.recorder.data.database.entities.Recording
import kotlinx.coroutines.flow.Flow

// repository pattern - abstracts database operations from viewmodels
class RecordingRepository(private val recordingDao: RecordingDao) {

    fun getAllRecordings(): Flow<List<Recording>> = recordingDao.getAllRecordings()

    fun getRecordingsByCategory(categoryId: Long): Flow<List<Recording>> =
        recordingDao.getRecordingsByCategory(categoryId)

    fun getUncategorizedRecordings(): Flow<List<Recording>> =
        recordingDao.getUncategorizedRecordings()

    fun getFavoriteRecordings(): Flow<List<Recording>> =
        recordingDao.getFavoriteRecordings()

    fun getDeletedRecordings(): Flow<List<Recording>> =
        recordingDao.getDeletedRecordings()

    suspend fun getRecordingById(id: Long): Recording? =
        recordingDao.getRecordingById(id)

    fun searchRecordings(query: String): Flow<List<Recording>> =
        recordingDao.searchRecordings(query)

    fun getRecordingsByDateRange(startDate: Long, endDate: Long): Flow<List<Recording>> =
        recordingDao.getRecordingsByDateRange(startDate, endDate)

    suspend fun insertRecording(recording: Recording): Long =
        recordingDao.insert(recording)

    suspend fun updateRecording(recording: Recording) =
        recordingDao.update(recording)

    suspend fun deleteRecording(recording: Recording) =
        recordingDao.delete(recording)

    suspend fun moveToRecycleBin(id: Long) =
        recordingDao.moveToRecycleBin(id, System.currentTimeMillis())

    suspend fun restoreFromRecycleBin(id: Long) =
        recordingDao.restoreFromRecycleBin(id)

    suspend fun permanentlyDeleteOldRecordings(cutoffTime: Long) =
        recordingDao.permanentlyDeleteOldRecordings(cutoffTime)

    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) =
        recordingDao.updateFavoriteStatus(id, isFavorite)

    suspend fun updateCategory(id: Long, categoryId: Long?) =
        recordingDao.updateCategory(id, categoryId)

    suspend fun updateTranscription(id: Long, transcription: String) =
        recordingDao.updateTranscription(id, transcription)
}