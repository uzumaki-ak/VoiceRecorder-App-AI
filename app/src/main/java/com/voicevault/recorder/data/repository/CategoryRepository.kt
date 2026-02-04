package com.voicevault.recorder.data.repository

import com.voicevault.recorder.data.database.CategoryDao
import com.voicevault.recorder.data.database.entities.Category
import kotlinx.coroutines.flow.Flow

// repository for category operations
class CategoryRepository(private val categoryDao: CategoryDao) {

    fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories()

    suspend fun getCategoryById(id: Long): Category? = categoryDao.getCategoryById(id)

    suspend fun getCategoryByName(name: String): Category? = categoryDao.getCategoryByName(name)

    fun getRecordingCountForCategory(categoryId: Long): Flow<Int> =
        categoryDao.getRecordingCountForCategory(categoryId)

    suspend fun insertCategory(category: Category): Long = categoryDao.insert(category)

    suspend fun updateCategory(category: Category) = categoryDao.update(category)

    suspend fun deleteCategory(category: Category) = categoryDao.delete(category)
}