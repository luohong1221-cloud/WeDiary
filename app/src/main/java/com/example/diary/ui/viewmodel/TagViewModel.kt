package com.example.diary.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.diary.data.database.DiaryDatabase
import com.example.diary.data.entity.Tag
import com.example.diary.data.repository.DiaryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TagViewModel(application: Application) : AndroidViewModel(application) {

    private val database = DiaryDatabase.getInstance(application)
    private val repository = DiaryRepository(
        database.diaryDao(),
        database.imageDao(),
        database.tagDao(),
        application
    )

    private val _uiState = MutableStateFlow(TagUiState())
    val uiState: StateFlow<TagUiState> = _uiState.asStateFlow()

    init {
        loadTags()
    }

    private fun loadTags() {
        viewModelScope.launch {
            repository.getAllTags()
                .catch { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
                .collect { tags ->
                    _uiState.update {
                        it.copy(
                            tags = tags,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    fun createTag(name: String, color: Long) {
        if (name.isBlank()) {
            _uiState.update { it.copy(error = "Tag name cannot be empty") }
            return
        }

        viewModelScope.launch {
            try {
                repository.createTag(name.trim(), color)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun updateTag(tag: Tag) {
        viewModelScope.launch {
            try {
                repository.updateTag(tag)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteTag(tagId: Long) {
        viewModelScope.launch {
            try {
                repository.deleteTag(tagId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class TagUiState(
    val tags: List<Tag> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)
