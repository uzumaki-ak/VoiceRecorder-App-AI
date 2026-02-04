package com.voicevault.recorder.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

// represents a bookmark/marker at a specific time in a recording
@Entity(
    tableName = "bookmarks",
    foreignKeys = [
        ForeignKey(
            entity = Recording::class,
            parentColumns = ["id"],
            childColumns = ["recordingId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Bookmark(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val recordingId: Long,
    val position: Long, // timestamp in milliseconds where bookmark is
    val createdAt: Long
)