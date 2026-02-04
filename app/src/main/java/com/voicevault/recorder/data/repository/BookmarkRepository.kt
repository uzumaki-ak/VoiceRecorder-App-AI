package com.voicevault.recorder.data.repository

import com.voicevault.recorder.data.database.BookmarkDao
import com.voicevault.recorder.data.database.entities.Bookmark
import kotlinx.coroutines.flow.Flow

// repository for bookmark operations
class BookmarkRepository(private val bookmarkDao: BookmarkDao) {

    fun getBookmarksForRecording(recordingId: Long): Flow<List<Bookmark>> =
        bookmarkDao.getBookmarksForRecording(recordingId)

    suspend fun getBookmarkById(id: Long): Bookmark? = bookmarkDao.getBookmarkById(id)

    suspend fun insertBookmark(bookmark: Bookmark): Long = bookmarkDao.insert(bookmark)

    suspend fun deleteBookmark(bookmark: Bookmark) = bookmarkDao.delete(bookmark)

    suspend fun deleteBookmarksForRecording(recordingId: Long) =
        bookmarkDao.deleteBookmarksForRecording(recordingId)
}