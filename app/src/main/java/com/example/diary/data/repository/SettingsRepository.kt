package com.example.diary.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "diary_settings")

class SettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        val USE_BIOMETRIC = booleanPreferencesKey("use_biometric")
        val PIN_CODE = stringPreferencesKey("pin_code")
        val DARK_MODE = stringPreferencesKey("dark_mode")
        val FIRST_LAUNCH = booleanPreferencesKey("first_launch")
        val DEFAULT_MOOD = stringPreferencesKey("default_mood")
        val AUTO_SAVE = booleanPreferencesKey("auto_save")
        val FONT_SIZE = stringPreferencesKey("font_size")
    }

    val appLockEnabled: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.APP_LOCK_ENABLED] ?: false
        }

    val useBiometric: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.USE_BIOMETRIC] ?: false
        }

    val pinCode: Flow<String?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.PIN_CODE]
        }

    val darkMode: Flow<DarkMode> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            val modeString = preferences[PreferencesKeys.DARK_MODE] ?: DarkMode.SYSTEM.name
            DarkMode.valueOf(modeString)
        }

    val isFirstLaunch: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.FIRST_LAUNCH] ?: true
        }

    val autoSave: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.AUTO_SAVE] ?: true
        }

    val fontSize: Flow<FontSize> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            val sizeString = preferences[PreferencesKeys.FONT_SIZE] ?: FontSize.MEDIUM.name
            FontSize.valueOf(sizeString)
        }

    suspend fun setAppLockEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.APP_LOCK_ENABLED] = enabled
        }
    }

    suspend fun setUseBiometric(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USE_BIOMETRIC] = enabled
        }
    }

    suspend fun setPinCode(pin: String?) {
        context.dataStore.edit { preferences ->
            if (pin != null) {
                preferences[PreferencesKeys.PIN_CODE] = pin
            } else {
                preferences.remove(PreferencesKeys.PIN_CODE)
            }
        }
    }

    suspend fun setDarkMode(mode: DarkMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DARK_MODE] = mode.name
        }
    }

    suspend fun setFirstLaunchComplete() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FIRST_LAUNCH] = false
        }
    }

    suspend fun setAutoSave(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_SAVE] = enabled
        }
    }

    suspend fun setFontSize(size: FontSize) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FONT_SIZE] = size.name
        }
    }

    suspend fun verifyPin(inputPin: String): Boolean {
        var storedPin: String? = null
        context.dataStore.data.collect { preferences ->
            storedPin = preferences[PreferencesKeys.PIN_CODE]
        }
        return storedPin == inputPin
    }
}

enum class DarkMode {
    LIGHT, DARK, SYSTEM
}

enum class FontSize(val scaleFactor: Float) {
    SMALL(0.85f),
    MEDIUM(1.0f),
    LARGE(1.15f),
    EXTRA_LARGE(1.3f)
}
