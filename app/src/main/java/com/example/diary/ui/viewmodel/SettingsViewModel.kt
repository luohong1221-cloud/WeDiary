package com.example.diary.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.diary.data.repository.DarkMode
import com.example.diary.data.repository.FontSize
import com.example.diary.data.repository.SettingsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            combine(
                settingsRepository.appLockEnabled,
                settingsRepository.useBiometric,
                settingsRepository.darkMode,
                settingsRepository.autoSave,
                settingsRepository.fontSize
            ) { appLock, biometric, darkMode, autoSave, fontSize ->
                SettingsUiState(
                    appLockEnabled = appLock,
                    useBiometric = biometric,
                    darkMode = darkMode,
                    autoSave = autoSave,
                    fontSize = fontSize,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun setAppLockEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAppLockEnabled(enabled)
        }
    }

    fun setUseBiometric(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setUseBiometric(enabled)
        }
    }

    fun setPinCode(pin: String) {
        viewModelScope.launch {
            settingsRepository.setPinCode(pin)
        }
    }

    fun clearPinCode() {
        viewModelScope.launch {
            settingsRepository.setPinCode(null)
        }
    }

    fun setDarkMode(mode: DarkMode) {
        viewModelScope.launch {
            settingsRepository.setDarkMode(mode)
        }
    }

    fun setAutoSave(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAutoSave(enabled)
        }
    }

    fun setFontSize(size: FontSize) {
        viewModelScope.launch {
            settingsRepository.setFontSize(size)
        }
    }
}

data class SettingsUiState(
    val appLockEnabled: Boolean = false,
    val useBiometric: Boolean = false,
    val darkMode: DarkMode = DarkMode.SYSTEM,
    val autoSave: Boolean = true,
    val fontSize: FontSize = FontSize.MEDIUM,
    val isLoading: Boolean = true
)
