package com.voicevault.recorder.ui.list

import android.app.Application
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.voicevault.recorder.VoiceVaultApp
import com.voicevault.recorder.data.database.entities.Category
import com.voicevault.recorder.data.database.entities.Recording
import com.voicevault.recorder.data.repository.CategoryRepository
import com.voicevault.recorder.data.repository.RecordingRepository
import com.voicevault.recorder.utils.FileUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

// viewmodel for list screen - manages recordings display and operations - ALL FUNCTIONS FIXED
class ListViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application
    private val recordingRepository: RecordingRepository
    private val categoryRepository: CategoryRepository

    init {
        val database = (application as VoiceVaultApp).database
        recordingRepository = RecordingRepository(database.recordingDao())
        categoryRepository = CategoryRepository(database.categoryDao())
    }

    // current filter
    private val _selectedFilter = MutableStateFlow<FilterType>(FilterType.All)
    val selectedFilter: StateFlow<FilterType> = _selectedFilter.asStateFlow()

    // FIXED: sort order
    private val _sortOrder = MutableStateFlow(SortOrder.DATE_DESC)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    // search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // selection mode
    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    private val _selectedRecordings = MutableStateFlow<Set<Long>>(emptySet())
    val selectedRecordings: StateFlow<Set<Long>> = _selectedRecordings.asStateFlow()

    // recordings based on current filter, search, and sort - FIXED
    val recordings: StateFlow<List<Recording>> = combine(
        _selectedFilter,
        _searchQuery,
        _sortOrder
    ) { filter, query, sort ->
        Triple(filter, query, sort)
    }.flatMapLatest { (filter, query, sort) ->
        val baseFlow = when {
            query.isNotEmpty() -> recordingRepository.searchRecordings(query)
            else -> when (filter) {
                is FilterType.All -> recordingRepository.getAllRecordings()
                is FilterType.Favorites -> recordingRepository.getFavoriteRecordings()
                is FilterType.Category -> recordingRepository.getRecordingsByCategory(filter.categoryId)
                is FilterType.Uncategorized -> recordingRepository.getUncategorizedRecordings()
                is FilterType.DateRange -> recordingRepository.getRecordingsByDateRange(
                    filter.startDate,
                    filter.endDate
                )
            }
        }

        baseFlow.map { list ->
            when (sort) {
                SortOrder.DATE_DESC -> list.sortedByDescending { it.createdAt }
                SortOrder.DATE_ASC -> list.sortedBy { it.createdAt }
                SortOrder.NAME_ASC -> list.sortedBy { it.fileName }
                SortOrder.NAME_DESC -> list.sortedByDescending { it.fileName }
                SortOrder.DURATION_DESC -> list.sortedByDescending { it.duration }
                SortOrder.DURATION_ASC -> list.sortedBy { it.duration }
            }
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // all categories
    val categories: StateFlow<List<Category>> = categoryRepository.getAllCategories()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    fun setFilter(filter: FilterType) {
        _selectedFilter.value = filter
    }

    // FIXED: sort order
    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleSelectionMode() {
        _isSelectionMode.value = !_isSelectionMode.value
        if (!_isSelectionMode.value) {
            _selectedRecordings.value = emptySet()
        }
    }

    fun toggleRecordingSelection(recordingId: Long) {
        _selectedRecordings.value = if (_selectedRecordings.value.contains(recordingId)) {
            _selectedRecordings.value - recordingId
        } else {
            _selectedRecordings.value + recordingId
        }
    }

    fun selectAllRecordings() {
        _selectedRecordings.value = recordings.value.map { it.id }.toSet()
    }

    fun clearSelection() {
        _selectedRecordings.value = emptySet()
    }

    // toggles favorite status for a recording
    fun toggleFavorite(recordingId: Long) {
        viewModelScope.launch {
            val recording = recordingRepository.getRecordingById(recordingId)
            recording?.let {
                recordingRepository.toggleFavorite(recordingId, !it.isFavorite)
            }
        }
    }

    // FIXED: toggle favorite for selected recordings
    fun toggleFavoriteForSelected(recordingIds: Set<Long>) {
        viewModelScope.launch {
            recordingIds.forEach { id ->
                val recording = recordingRepository.getRecordingById(id)
                recording?.let {
                    recordingRepository.toggleFavorite(id, !it.isFavorite)
                }
            }
        }
    }

    // moves recordings to different category
    fun moveToCategory(recordingIds: Set<Long>, categoryId: Long?) {
        viewModelScope.launch {
            recordingIds.forEach { id ->
                recordingRepository.updateCategory(id, categoryId)
            }
            clearSelection()
            toggleSelectionMode()
        }
    }

    // deletes selected recordings
    fun deleteRecordings(recordingIds: Set<Long>, permanent: Boolean = false) {
        viewModelScope.launch {
            recordingIds.forEach { id ->
                if (permanent) {
                    val recording = recordingRepository.getRecordingById(id)
                    recording?.let {
                        FileUtils.deleteFile(it.filePath)
                        recordingRepository.deleteRecording(it)
                    }
                } else {
                    recordingRepository.moveToRecycleBin(id)
                }
            }
            clearSelection()
            toggleSelectionMode()
        }
    }

    // FIXED: share recordings function
    fun shareRecordings(recordingIds: Set<Long>) {
        viewModelScope.launch {
            val files = mutableListOf<File>()
            recordingIds.forEach { id ->
                val recording = recordingRepository.getRecordingById(id)
                recording?.let {
                    files.add(File(it.filePath))
                }
            }

            if (files.isNotEmpty()) {
                val uris = files.map { file ->
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        file
                    )
                }

                val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                    type = "audio/*"
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                context.startActivity(Intent.createChooser(shareIntent, "Share recordings").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            }
        }
    }

    // renames a recording
    fun renameRecording(recordingId: Long, newName: String) {
        viewModelScope.launch {
            val recording = recordingRepository.getRecordingById(recordingId)
            recording?.let {
                recordingRepository.updateRecording(it.copy(fileName = newName))
            }
        }
    }

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

    // deletes a category
    fun deleteCategory(categoryId: Long) {
        viewModelScope.launch {
            val category = categoryRepository.getCategoryById(categoryId)
            category?.let {
                categoryRepository.deleteCategory(it)
            }
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
}

// filter types for recordings list
sealed class FilterType {
    object All : FilterType()
    object Favorites : FilterType()
    object Uncategorized : FilterType()
    data class Category(val categoryId: Long) : FilterType()
    data class DateRange(val startDate: Long, val endDate: Long) : FilterType()
}

// FIXED: sort order enum
enum class SortOrder {
    DATE_DESC,
    DATE_ASC,
    NAME_ASC,
    NAME_DESC,
    DURATION_DESC,
    DURATION_ASC
}