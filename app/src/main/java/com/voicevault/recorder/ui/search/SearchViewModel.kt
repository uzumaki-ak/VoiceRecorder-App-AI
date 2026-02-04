package com.voicevault.recorder.ui.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.voicevault.recorder.VoiceVaultApp
import com.voicevault.recorder.data.database.entities.Category
import com.voicevault.recorder.data.database.entities.Recording
import com.voicevault.recorder.data.repository.CategoryRepository
import com.voicevault.recorder.data.repository.RecordingRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

// viewmodel for search screen
class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val recordingRepository: RecordingRepository
    private val categoryRepository: CategoryRepository

    init {
        val database = (application as VoiceVaultApp).database
        recordingRepository = RecordingRepository(database.recordingDao())
        categoryRepository = CategoryRepository(database.categoryDao())
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _dateFilter = MutableStateFlow<DateFilter?>(null)
    val dateFilter: StateFlow<DateFilter?> = _dateFilter.asStateFlow()

    private val _categoryFilter = MutableStateFlow<String?>(null)
    val categoryFilter: StateFlow<String?> = _categoryFilter.asStateFlow()

    // search results based on query and filters
    val searchResults: StateFlow<List<Recording>> = combine(
        _searchQuery,
        _dateFilter,
        _categoryFilter
    ) { query, dateFilter, categoryFilter ->
        Triple(query, dateFilter, categoryFilter)
    }.flatMapLatest { (query, dateFilter, categoryFilter) ->
        if (query.isEmpty()) {
            flowOf(emptyList())
        } else {
            recordingRepository.searchRecordings(query).map { recordings ->
                recordings.filter { recording ->
                    // apply date filter
                    val passesDateFilter = when (dateFilter) {
                        DateFilter.YESTERDAY -> {
                            val yesterday = Calendar.getInstance().apply {
                                add(Calendar.DAY_OF_YEAR, -1)
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                            }.timeInMillis
                            recording.createdAt >= yesterday
                        }
                        DateFilter.PAST_7_DAYS -> {
                            val sevenDaysAgo = Calendar.getInstance().apply {
                                add(Calendar.DAY_OF_YEAR, -7)
                            }.timeInMillis
                            recording.createdAt >= sevenDaysAgo
                        }
                        DateFilter.PAST_30_DAYS -> {
                            val thirtyDaysAgo = Calendar.getInstance().apply {
                                add(Calendar.DAY_OF_YEAR, -30)
                            }.timeInMillis
                            recording.createdAt >= thirtyDaysAgo
                        }
                        null -> true
                    }

                    // apply category filter
                    val passesCategoryFilter = when (categoryFilter) {
                        "Uncategorised" -> recording.categoryId == null
                        "Call recording" -> {
                            // check if recording is in call recording category
                            // for now just check if uncategorised
                            recording.categoryId == null
                        }
                        null -> true
                        else -> false
                    }

                    passesDateFilter && passesCategoryFilter
                }
            }
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val categories: StateFlow<List<Category>> = categoryRepository.getAllCategories()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setDateFilter(filter: DateFilter?) {
        _dateFilter.value = filter
    }

    fun setCategoryFilter(filter: String?) {
        _categoryFilter.value = filter
    }

    fun clearFilters() {
        _dateFilter.value = null
        _categoryFilter.value = null
    }

    fun toggleFavorite(recordingId: Long) {
        viewModelScope.launch {
            val recording = recordingRepository.getRecordingById(recordingId)
            recording?.let {
                recordingRepository.toggleFavorite(recordingId, !it.isFavorite)
            }
        }
    }
}

enum class DateFilter {
    YESTERDAY,
    PAST_7_DAYS,
    PAST_30_DAYS
}