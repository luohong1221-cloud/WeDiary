package com.example.diary.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.diary.data.database.DiaryDatabase
import com.example.diary.data.entity.*
import com.example.diary.data.repository.DiaryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class EditDiaryViewModel(application: Application) : AndroidViewModel(application) {

    private val database = DiaryDatabase.getInstance(application)
    private val repository = DiaryRepository(
        database.diaryDao(),
        database.imageDao(),
        database.tagDao(),
        application
    )

    private val _uiState = MutableStateFlow(EditDiaryUiState())
    val uiState: StateFlow<EditDiaryUiState> = _uiState.asStateFlow()

    private var originalDiary: DiaryEntry? = null

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

    fun loadDiary(diaryId: Long) {
        if (diaryId == 0L) {
            _uiState.update { it.copy(isLoading = false, isNewDiary = true) }
            return
        }

        viewModelScope.launch {
            try {
                val diaryWithDetails = repository.getDiaryWithDetailsById(diaryId)
                if (diaryWithDetails != null) {
                    originalDiary = diaryWithDetails.diary
                    _uiState.update {
                        it.copy(
                            title = diaryWithDetails.diary.title,
                            content = diaryWithDetails.diary.content,
                            mood = diaryWithDetails.diary.mood,
                            weather = diaryWithDetails.diary.weather,
                            location = diaryWithDetails.diary.location,
                            images = diaryWithDetails.images,
                            selectedTags = diaryWithDetails.tags,
                            isLoading = false,
                            isNewDiary = false,
                            diaryId = diaryId
                        )
                    }
                } else {
                    _uiState.update { it.copy(error = "Diary not found", isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title, hasChanges = true) }
    }

    fun updateContent(content: String) {
        _uiState.update { it.copy(content = content, hasChanges = true) }
    }

    fun updateMood(mood: Mood) {
        _uiState.update { it.copy(mood = mood, hasChanges = true) }
    }

    fun updateWeather(weather: Weather?) {
        _uiState.update { it.copy(weather = weather, hasChanges = true) }
    }

    fun updateLocation(location: String?) {
        _uiState.update { it.copy(location = location, hasChanges = true) }
    }

    fun addImages(uris: List<Uri>) {
        _uiState.update {
            it.copy(
                pendingImageUris = it.pendingImageUris + uris,
                hasChanges = true
            )
        }
    }

    fun removeImage(image: DiaryImage) {
        viewModelScope.launch {
            try {
                repository.deleteImage(image)
                _uiState.update {
                    it.copy(
                        images = it.images.filter { img -> img.id != image.id },
                        hasChanges = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun removePendingImage(uri: Uri) {
        _uiState.update {
            it.copy(
                pendingImageUris = it.pendingImageUris.filter { u -> u != uri },
                hasChanges = true
            )
        }
    }

    fun reorderImages(fromIndex: Int, toIndex: Int) {
        val currentState = _uiState.value
        val newList = currentState.images.toMutableList()
        val item = newList.removeAt(fromIndex)
        newList.add(toIndex, item)

        _uiState.update { it.copy(images = newList, hasChanges = true) }

        viewModelScope.launch {
            try {
                repository.updateImagesSortOrder(newList.mapIndexed { index, image ->
                    image.copy(sortOrder = index)
                })
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun toggleTag(tag: Tag) {
        _uiState.update { state ->
            val currentTags = state.selectedTags.toMutableList()
            if (currentTags.any { it.id == tag.id }) {
                currentTags.removeAll { it.id == tag.id }
            } else {
                currentTags.add(tag)
            }
            state.copy(selectedTags = currentTags, hasChanges = true)
        }
    }

    fun createAndSelectTag(name: String, color: Long) {
        viewModelScope.launch {
            try {
                val newTag = repository.createTag(name, color)
                _uiState.update { state ->
                    state.copy(
                        selectedTags = state.selectedTags + newTag,
                        hasChanges = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun saveDiary(onSuccess: (Long) -> Unit) {
        val state = _uiState.value

        if (state.title.isBlank() && state.content.isBlank()) {
            _uiState.update { it.copy(error = "Please enter a title or content") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val diary = DiaryEntry(
                    id = state.diaryId,
                    title = state.title,
                    content = state.content,
                    mood = state.mood,
                    weather = state.weather,
                    location = state.location,
                    createdAt = originalDiary?.createdAt ?: System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    isFavorite = originalDiary?.isFavorite ?: false
                )

                val diaryId = repository.saveDiary(
                    diary = diary,
                    imageUris = state.pendingImageUris,
                    tagIds = state.selectedTags.map { it.id }
                )

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        hasChanges = false,
                        diaryId = diaryId
                    )
                }
                onSuccess(diaryId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isSaving = false) }
            }
        }
    }

    fun hasUnsavedChanges(): Boolean = _uiState.value.hasChanges

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class EditDiaryUiState(
    val diaryId: Long = 0L,
    val title: String = "",
    val content: String = "",
    val mood: Mood = Mood.NEUTRAL,
    val weather: Weather? = null,
    val location: String? = null,
    val images: List<DiaryImage> = emptyList(),
    val pendingImageUris: List<Uri> = emptyList(),
    val selectedTags: List<Tag> = emptyList(),
    val availableTags: List<Tag> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isNewDiary: Boolean = true,
    val hasChanges: Boolean = false,
    val error: String? = null
)
