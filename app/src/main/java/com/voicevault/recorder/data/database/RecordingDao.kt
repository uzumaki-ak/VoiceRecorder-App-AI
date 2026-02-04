package com.voicevault.recorder.data.database

import androidx.room.*
import com.voicevault.recorder.data.database.entities.Recording
import kotlinx.coroutines.flow.Flow

// data access object for recording operations - room generates the implementation
@Dao
interface RecordingDao {

    // inserts a new recording and returns its id
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recording: Recording): Long

    // updates existing recording
    @Update
    suspend fun update(recording: Recording)

    // deletes a recording
    @Delete
    suspend fun delete(recording: Recording)

    // gets all recordings that aren't deleted, sorted by creation date
    @Query("SELECT * FROM recordings WHERE isDeleted = 0 ORDER BY createdAt DESC")
    fun getAllRecordings(): Flow<List<Recording>>

    // gets recordings by category
    @Query("SELECT * FROM recordings WHERE categoryId = :categoryId AND isDeleted = 0 ORDER BY createdAt DESC")
    fun getRecordingsByCategory(categoryId: Long): Flow<List<Recording>>

    // gets uncategorized recordings
    @Query("SELECT * FROM recordings WHERE categoryId IS NULL AND isDeleted = 0 ORDER BY createdAt DESC")
    fun getUncategorizedRecordings(): Flow<List<Recording>>

    // gets favorite recordings
    @Query("SELECT * FROM recordings WHERE isFavorite = 1 AND isDeleted = 0 ORDER BY createdAt DESC")
    fun getFavoriteRecordings(): Flow<List<Recording>>

    // gets deleted recordings for recycle bin
    @Query("SELECT * FROM recordings WHERE isDeleted = 1 ORDER BY deletedAt DESC")
    fun getDeletedRecordings(): Flow<List<Recording>>

    // gets a single recording by id
    @Query("SELECT * FROM recordings WHERE id = :id")
    suspend fun getRecordingById(id: Long): Recording?

    // searches recordings by filename
    @Query("SELECT * FROM recordings WHERE fileName LIKE '%' || :query || '%' AND isDeleted = 0 ORDER BY createdAt DESC")
    fun searchRecordings(query: String): Flow<List<Recording>>

    // gets recordings created in a date range
    @Query("SELECT * FROM recordings WHERE createdAt BETWEEN :startDate AND :endDate AND isDeleted = 0 ORDER BY createdAt DESC")
    fun getRecordingsByDateRange(startDate: Long, endDate: Long): Flow<List<Recording>>

    // marks recording as deleted instead of actually deleting it
    @Query("UPDATE recordings SET isDeleted = 1, deletedAt = :deletedAt WHERE id = :id")
    suspend fun moveToRecycleBin(id: Long, deletedAt: Long)

    // restores recording from recycle bin
    @Query("UPDATE recordings SET isDeleted = 0, deletedAt = NULL WHERE id = :id")
    suspend fun restoreFromRecycleBin(id: Long)

    // permanently deletes old recordings from recycle bin
    @Query("DELETE FROM recordings WHERE isDeleted = 1 AND deletedAt < :cutoffTime")
    suspend fun permanentlyDeleteOldRecordings(cutoffTime: Long)

    // toggles favorite status
    @Query("UPDATE recordings SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean)

    // updates category
    @Query("UPDATE recordings SET categoryId = :categoryId WHERE id = :id")
    suspend fun updateCategory(id: Long, categoryId: Long?)

    // updates transcription
    @Query("UPDATE recordings SET transcription = :transcription WHERE id = :id")
    suspend fun updateTranscription(id: Long, transcription: String)
}