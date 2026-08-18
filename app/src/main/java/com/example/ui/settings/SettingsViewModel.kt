package com.example.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.datastore.UserPreferencesDataStore
import com.example.domain.model.ApiKeySlot
import com.example.domain.model.ApiProvider
import com.example.domain.usecase.ClearPromptHistoryUseCase
import com.example.domain.usecase.ExportPromptHistoryUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferencesDataStore: UserPreferencesDataStore,
    private val exportPromptHistoryUseCase: ExportPromptHistoryUseCase,
    private val clearPromptHistoryUseCase: ClearPromptHistoryUseCase
) : ViewModel() {

    val apiSlots: StateFlow<List<ApiKeySlot>> = preferencesDataStore.apiSlotsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val autoFallback: StateFlow<Boolean> = preferencesDataStore.autoFallbackFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val temperature: StateFlow<Float> = preferencesDataStore.temperatureFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.5f)

    val autoSave: StateFlow<Boolean> = preferencesDataStore.autoSaveFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isDarkTheme: StateFlow<Boolean?> = preferencesDataStore.isDarkThemeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun updateSlot(
        slotIndex: Int,
        provider: ApiProvider? = null,
        apiKey: String? = null,
        model: String? = null,
        endpoint: String? = null,
        label: String? = null,
        isEnabled: Boolean? = null
    ) {
        viewModelScope.launch {
            preferencesDataStore.updateSlot(
                slotIndex = slotIndex,
                provider = provider,
                apiKey = apiKey,
                model = model,
                endpoint = endpoint,
                label = label,
                isEnabled = isEnabled
            )
        }
    }

    fun setAutoFallback(enabled: Boolean) {
        viewModelScope.launch {
            preferencesDataStore.setAutoFallback(enabled)
        }
    }

    fun updateTemperature(temp: Float) {
        viewModelScope.launch {
            preferencesDataStore.setTemperature(temp)
        }
    }

    fun updateAutoSave(enabled: Boolean) {
        viewModelScope.launch {
            preferencesDataStore.setAutoSave(enabled)
        }
    }

    fun updateDarkTheme(isDark: Boolean?) {
        viewModelScope.launch {
            preferencesDataStore.setDarkTheme(isDark)
        }
    }

    fun exportHistoryJson(onExported: (String) -> Unit) {
        viewModelScope.launch {
            val json = exportPromptHistoryUseCase().first()
            onExported(json)
        }
    }

    fun clearAllHistory(onSuccess: () -> Unit) {
        viewModelScope.launch {
            clearPromptHistoryUseCase()
            onSuccess()
        }
    }

    companion object {
        fun provideFactory(
            preferencesDataStore: UserPreferencesDataStore,
            exportPromptHistoryUseCase: ExportPromptHistoryUseCase,
            clearPromptHistoryUseCase: ClearPromptHistoryUseCase
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(
                    preferencesDataStore = preferencesDataStore,
                    exportPromptHistoryUseCase = exportPromptHistoryUseCase,
                    clearPromptHistoryUseCase = clearPromptHistoryUseCase
                ) as T
            }
        }
    }
}
