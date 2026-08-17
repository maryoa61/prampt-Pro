package com.example.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesDataStore(private val context: Context) {

    companion object {
        val KEY_MODEL = stringPreferencesKey("gemini_model")
        val KEY_TEMPERATURE = floatPreferencesKey("temperature")
        val KEY_AUTO_SAVE = booleanPreferencesKey("auto_save_prompts")
        val KEY_THEME_DARK = booleanPreferencesKey("is_dark_theme")
    }

    val selectedModelFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_MODEL] ?: "gemini-2.5-flash"
    }

    val temperatureFlow: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[KEY_TEMPERATURE] ?: 0.5f
    }

    val autoSaveFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_AUTO_SAVE] ?: true
    }

    val isDarkThemeFlow: Flow<Boolean?> = context.dataStore.data.map { preferences ->
        preferences[KEY_THEME_DARK]
    }

    suspend fun setModel(model: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_MODEL] = model
        }
    }

    suspend fun setTemperature(temperature: Float) {
        context.dataStore.edit { preferences ->
            preferences[KEY_TEMPERATURE] = temperature
        }
    }

    suspend fun setAutoSave(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_AUTO_SAVE] = enabled
        }
    }

    suspend fun setDarkTheme(isDark: Boolean?) {
        context.dataStore.edit { preferences ->
            if (isDark == null) {
                preferences.remove(KEY_THEME_DARK)
            } else {
                preferences[KEY_THEME_DARK] = isDark
            }
        }
    }
}
