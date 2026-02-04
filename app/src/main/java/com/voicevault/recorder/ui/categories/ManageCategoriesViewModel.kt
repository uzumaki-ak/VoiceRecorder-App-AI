package com.voicevault.recorder.ui.categories

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.voicevault.recorder.VoiceVaultApp
import com.voicevault.recorder.data.database.entities.Category
import com.voicevault.recorder.data.repository.CategoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// viewmodel for manage categories screen - handles all category operations
class ManageCategoriesViewModel(application: Application) : AndroidViewModel(application) {

    private val categoryRepository: CategoryRepository

    init {
        val database = (application as VoiceVaultApp).database
        categoryRepository = CategoryRepository(database.categoryDao())
    }

    // all categories
    val categories: StateFlow<List<Category>> = categoryRepository.getAllCategories()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    // creates new category
    fun createCategory(name: String) {
        viewModelScope.launch {
            val category = Category(
                name = name,
                createdAt = System.currentTimeMillis()
            )
            categoryRepository.insertCategory(category)
        }
    }

    // renames a category
    fun renameCategory(categoryId: Long, newName: String) {
        viewModelScope.launch {
            val category = categoryRepository.getCategoryById(categoryId)
            category?.let {
                categoryRepository.updateCategory(it.copy(name = newName))
            }
        }
    }

    // deletes a category
    fun deleteCategory(categoryId: Long) {
        viewModelScope.launch {
            val category = categoryRepository.getCategoryById(categoryId)
            category?.let {
                categoryRepository.deleteCategory(it)
            }
        }
    }
}