package com.voicevault.recorder.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

// represents a single audio recording in the database
@Entity(tableName = "recordings")
data class Recording(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fileName: String,
    val filePath: String,
    val duration: Long, // in milliseconds
    val fileSize: Long, // in bytes
    val createdAt: Long, // timestamp
    val categoryId: Long? = null, // null means uncategorized
    val isFavorite: Boolean = false,
    val isDeleted: Boolean = false, // for recycle bin
    val deletedAt: Long? = null,
    val transcription: String? = null, // vosk transcription result
    val bitrate: Int,
    val sampleRate: Int
)