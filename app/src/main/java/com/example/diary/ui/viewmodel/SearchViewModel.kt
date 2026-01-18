package com.example.diary.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.diary.data.database.DiaryDatabase
import com.example.diary.data.entity.DiaryEntry
import com.example.diary.data.entity.Mood
import com.example.diary.data.entity.Tag
import com.example.diary.data.repository.DiaryRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val database = DiaryDatabase.getInstance(application)
    private val repository = DiaryRepository(
        database.diaryDao(),
        database.imageDao(),
        database.tagDao(),
        application
    )

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadAllTags()
    }

    private fun loadAllTags() {
        viewModelScope.launch {
            repository.getAllTags().collect { tags ->
                _uiState.update { it.copy(availableTags = tags) }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        debounceSearch(query)
    }

    private fun debounceSearch(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300) // Debounce 300ms
            performSearch(query)
        }
    }

    private fun performSearch(query: String) {
        if (query.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            try {
                repository.searchDiaries(query)
                    .catch { e ->
                        _uiState.update { it.copy(error = e.message, isSearching = false) }
                    }
                    .collect { results ->
                        val filteredResults = applyFilters(results)
                        _uiState.update {
                            it.copy(
                                searchResults = filteredResults,
                                isSearching = false,
                                error = null
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isSearching = false) }
            }
        }
    }

    private fun applyFilters(results: List<DiaryEntry>): List<DiaryEntry> {
        val state = _uiState.value
        var filtered = results

        // Filter by mood
        if (state.selectedMoods.isNotEmpty()) {
            filtered = filtered.filter { it.mood in state.selectedMoods }
        }

        // Filter by date range
        state.dateRangeStart?.let { start ->
            filtered = filtered.filter { it.createdAt >= start }
        }
        state.dateRangeEnd?.let { end ->
            filtered = filtered.filter { it.createdAt <= end }
        }

        return filtered
    }

    fun toggleMoodFilter(mood: Mood) {
        _uiState.update { state ->
            val currentMoods = state.selectedMoods.toMutableSet()
            if (mood in currentMoods) {
                currentMoods.remove(mood)
            } else {
                currentMoods.add(mood)
            }
            state.copy(selectedMoods = currentMoods)
        }
        // Re-search with new filters
        performSearch(_uiState.value.searchQuery)
    }

    fun toggleTagFilter(tag: Tag) {
        _uiState.update { state ->
            val currentTags = state.selectedTags.toMutableSet()
            if (tag in currentTags) {
                currentTags.remove(tag)
            } else {
                currentTags.add(tag)
            }
            state.copy(selectedTags = currentTags)
        }
        performSearch(_uiState.value.searchQuery)
    }

    fun setDateRange(start: Long?, end: Long?) {
        _uiState.update { it.copy(dateRangeStart = start, dateRangeEnd = end) }
        performSearch(_uiState.value.searchQuery)
    }

    fun clearFilters() {
        _uiState.update {
            it.copy(
                selectedMoods = emptySet(),
                selectedTags = emptySet(),
                dateRangeStart = null,
                dateRangeEnd = null
            )
        }
        performSearch(_uiState.value.searchQuery)
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class SearchUiState(
    val searchQuery: String = "",
    val searchResults: List<DiaryEntry> = emptyList(),
    val isSearching: Boolean = false,
    val selectedMoods: Set<Mood> = emptySet(),
    val selectedTags: Set<Tag> = emptySet(),
    val availableTags: List<Tag> = emptyList(),
    val dateRangeStart: Long? = null,
    val dateRangeEnd: Long? = null,
    val error: String? = null
)
