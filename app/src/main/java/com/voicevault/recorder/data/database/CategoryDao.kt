package com.voicevault.recorder.data.database

import androidx.room.*
import com.voicevault.recorder.data.database.entities.Category
import kotlinx.coroutines.flow.Flow

// data access object for category operations
@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: Category): Long

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)

    // gets all categories sorted by name
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<Category>>

    // gets category by id
    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Long): Category?

    // gets category by name
    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    suspend fun getCategoryByName(name: String): Category?

    // counts recordings in a category
    @Query("SELECT COUNT(*) FROM recordings WHERE categoryId = :categoryId AND isDeleted = 0")
    fun getRecordingCountForCategory(categoryId: Long): Flow<Int>
}