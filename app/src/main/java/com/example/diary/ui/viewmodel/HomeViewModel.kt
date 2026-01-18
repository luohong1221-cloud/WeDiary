package com.example.diary.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.diary.data.database.DiaryDatabase
import com.example.diary.data.entity.DiaryWithDetails
import com.example.diary.data.repository.DiaryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val database = DiaryDatabase.getInstance(application)
    private val repository = DiaryRepository(
        database.diaryDao(),
        database.imageDao(),
        database.tagDao(),
        application
    )

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDiaries()
        loadDiaryCount()
    }

    private fun loadDiaries() {
        viewModelScope.launch {
            repository.getAllDiariesWithDetails()
                .catch { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
                .collect { diaries ->
                    _uiState.update {
                        it.copy(
                            diaries = diaries,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    private fun loadDiaryCount() {
        viewModelScope.launch {
            repository.getDiaryCount().collect { count ->
                _uiState.update { it.copy(totalCount = count) }
            }
        }
    }

    fun deleteDiary(diaryId: Long) {
        viewModelScope.launch {
            try {
                repository.deleteDiary(diaryId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun toggleFavorite(diaryId: Long, currentFavorite: Boolean) {
        viewModelScope.launch {
            try {
                repository.toggleFavorite(diaryId, !currentFavorite)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class HomeUiState(
    val diaries: List<DiaryWithDetails> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val totalCount: Int = 0
)
