package com.voicevault.recorder.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.voicevault.recorder.data.database.entities.Bookmark
import com.voicevault.recorder.data.database.entities.Category
import com.voicevault.recorder.data.database.entities.Recording
import com.voicevault.recorder.utils.Constants

// main database class - defines all entities and provides dao access
@Database(
    entities = [Recording::class, Category::class, Bookmark::class],
    version = Constants.DATABASE_VERSION,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun recordingDao(): RecordingDao
    abstract fun categoryDao(): CategoryDao
    abstract fun bookmarkDao(): BookmarkDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // singleton pattern - only one database instance exists
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    Constants.DATABASE_NAME
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}