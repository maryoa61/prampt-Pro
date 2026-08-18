package com.example.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.model.ApiKeySlot
import com.example.domain.model.ApiProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesDataStore(private val context: Context) {

    companion object {
        val KEY_AUTO_FALLBACK = booleanPreferencesKey("auto_fallback_enabled")
        val KEY_TEMPERATURE = floatPreferencesKey("temperature")
        val KEY_AUTO_SAVE = booleanPreferencesKey("auto_save_prompts")
        val KEY_THEME_DARK = booleanPreferencesKey("is_dark_theme")

        // 4 Multi-API Slots (0 = Primary, 1 = Backup 1, 2 = Backup 2, 3 = Backup 3)
        fun slotProviderKey(slot: Int) = stringPreferencesKey("slot_${slot}_provider")
        fun slotKeyKey(slot: Int) = stringPreferencesKey("slot_${slot}_api_key")
        fun slotModelKey(slot: Int) = stringPreferencesKey("slot_${slot}_model")
        fun slotEndpointKey(slot: Int) = stringPreferencesKey("slot_${slot}_endpoint")
        fun slotLabelKey(slot: Int) = stringPreferencesKey("slot_${slot}_label")
        fun slotEnabledKey(slot: Int) = booleanPreferencesKey("slot_${slot}_enabled")
    }

    val autoFallbackFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_AUTO_FALLBACK] ?: true
    }

    val apiSlotsFlow: Flow<List<ApiKeySlot>> = context.dataStore.data.map { preferences ->
        (0 until 4).map { index ->
            val defaultLabel = when (index) {
                0 -> "Primary Engine (بات اصلی)"
                1 -> "Backup 1 (ذخیره ۱)"
                2 -> "Backup 2 (ذخیره ۲)"
                3 -> "Backup 3 (ذخیره ۳)"
                else -> "Slot $index"
            }
            val defaultProvider = when (index) {
                0 -> ApiProvider.GEMINI
                1 -> ApiProvider.OPENAI
                2 -> ApiProvider.CLAUDE
                3 -> ApiProvider.DEEPSEEK
                else -> ApiProvider.GEMINI
            }

            val providerId = preferences[slotProviderKey(index)] ?: defaultProvider.id
            val provider = ApiProvider.fromId(providerId)
            val apiKey = preferences[slotKeyKey(index)] ?: ""
            val model = preferences[slotModelKey(index)] ?: provider.defaultModel
            val endpoint = preferences[slotEndpointKey(index)] ?: ""
            val label = preferences[slotLabelKey(index)] ?: defaultLabel
            val isEnabled = preferences[slotEnabledKey(index)] ?: true

            ApiKeySlot(
                slotIndex = index,
                label = label,
                provider = provider,
                apiKey = apiKey,
                model = model,
                customEndpoint = endpoint,
                isEnabled = isEnabled
            )
        }
    }

    val primarySlotFlow: Flow<ApiKeySlot> = apiSlotsFlow.map { it.first() }

    val temperatureFlow: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[KEY_TEMPERATURE] ?: 0.5f
    }

    val autoSaveFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_AUTO_SAVE] ?: true
    }

    val isDarkThemeFlow: Flow<Boolean?> = context.dataStore.data.map { preferences ->
        preferences[KEY_THEME_DARK]
    }

    suspend fun updateSlot(
        slotIndex: Int,
        provider: ApiProvider? = null,
        apiKey: String? = null,
        model: String? = null,
        endpoint: String? = null,
        label: String? = null,
        isEnabled: Boolean? = null
    ) {
        context.dataStore.edit { preferences ->
            if (provider != null) {
                preferences[slotProviderKey(slotIndex)] = provider.id
                preferences[slotModelKey(slotIndex)] = provider.defaultModel
                if (provider.defaultEndpoint.isNotBlank()) {
                    preferences[slotEndpointKey(slotIndex)] = provider.defaultEndpoint
                }
            }
            if (apiKey != null) {
                preferences[slotKeyKey(slotIndex)] = apiKey.trim()
            }
            if (model != null) {
                preferences[slotModelKey(slotIndex)] = model.trim()
            }
            if (endpoint != null) {
                preferences[slotEndpointKey(slotIndex)] = endpoint.trim()
            }
            if (label != null) {
                preferences[slotLabelKey(slotIndex)] = label.trim()
            }
            if (isEnabled != null) {
                preferences[slotEnabledKey(slotIndex)] = isEnabled
            }
        }
    }

    suspend fun setAutoFallback(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_AUTO_FALLBACK] = enabled
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
