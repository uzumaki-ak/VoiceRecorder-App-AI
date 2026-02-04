package com.voicevault.recorder.data.database

import androidx.room.*
import com.voicevault.recorder.data.database.entities.Bookmark
import kotlinx.coroutines.flow.Flow

// data access object for bookmark operations
@Dao
interface BookmarkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bookmark: Bookmark): Long

    @Delete
    suspend fun delete(bookmark: Bookmark)

    // gets all bookmarks for a recording, sorted by position
    @Query("SELECT * FROM bookmarks WHERE recordingId = :recordingId ORDER BY position ASC")
    fun getBookmarksForRecording(recordingId: Long): Flow<List<Bookmark>>

    // deletes all bookmarks for a recording
    @Query("DELETE FROM bookmarks WHERE recordingId = :recordingId")
    suspend fun deleteBookmarksForRecording(recordingId: Long)

    // gets bookmark by id
    @Query("SELECT * FROM bookmarks WHERE id = :id")
    suspend fun getBookmarkById(id: Long): Bookmark?
}