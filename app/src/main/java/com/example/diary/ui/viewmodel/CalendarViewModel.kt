package com.example.diary.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.diary.data.database.DiaryDatabase
import com.example.diary.data.entity.DiaryWithDetails
import com.example.diary.data.repository.DiaryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.*
import java.time.format.DateTimeFormatter

class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val database = DiaryDatabase.getInstance(application)
    private val repository = DiaryRepository(
        database.diaryDao(),
        database.imageDao(),
        database.tagDao(),
        application
    )

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadDatesWithEntries()
        loadDiariesForSelectedDate()
    }

    private fun loadDatesWithEntries() {
        viewModelScope.launch {
            repository.getDatesWithEntries()
                .catch { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
                .collect { dates ->
                    val localDates = dates.mapNotNull { dateString ->
                        try {
                            LocalDate.parse(dateString)
                        } catch (e: Exception) {
                            null
                        }
                    }.toSet()
                    _uiState.update { it.copy(datesWithEntries = localDates) }
                }
        }
    }

    fun loadDiariesForSelectedDate() {
        val selectedDate = _uiState.value.selectedDate
        val startOfDay = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = selectedDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getDiariesBetween(startOfDay, endOfDay)
                .catch { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
                .collect { diaries ->
                    _uiState.update {
                        it.copy(
                            diariesForSelectedDate = diaries,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    fun selectDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
        loadDiariesForSelectedDate()
    }

    fun navigateMonth(delta: Int) {
        _uiState.update { state ->
            val newMonth = state.currentMonth.plusMonths(delta.toLong())
            state.copy(currentMonth = newMonth)
        }
    }

    fun goToToday() {
        val today = LocalDate.now()
        _uiState.update {
            it.copy(
                selectedDate = today,
                currentMonth = YearMonth.from(today)
            )
        }
        loadDiariesForSelectedDate()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class CalendarUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val datesWithEntries: Set<LocalDate> = emptySet(),
    val diariesForSelectedDate: List<DiaryWithDetails> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
