package com.voicevault.recorder.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

// represents a category that recordings can be organized into
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdAt: Long
)