package com.example.ui.generator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.datastore.UserPreferencesDataStore
import com.example.domain.model.ApiKeySlot
import com.example.domain.model.GeneratedPrompt
import com.example.domain.model.GenerationResult
import com.example.domain.model.PromptStyle
import com.example.domain.model.UserPromptInput
import com.example.domain.usecase.GeneratePromptUseCase
import com.example.domain.usecase.SavePromptUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GeneratorUiState(
    val rawInput: String = "",
    val selectedStyle: PromptStyle = PromptStyle.GENERAL,
    val customRole: String = "",
    val customConstraints: String = "",
    val isAdvancedExpanded: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val generatedPrompt: GeneratedPrompt? = null,
    val generationResult: GenerationResult? = null,
    val isSaved: Boolean = false
)

class GeneratorViewModel(
    private val generatePromptUseCase: GeneratePromptUseCase,
    private val savePromptUseCase: SavePromptUseCase,
    private val preferencesDataStore: UserPreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(GeneratorUiState())
    val uiState: StateFlow<GeneratorUiState> = _uiState.asStateFlow()

    val apiSlots: StateFlow<List<ApiKeySlot>> = preferencesDataStore.apiSlotsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val primarySlot: StateFlow<ApiKeySlot?> = preferencesDataStore.primarySlotFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isAutoFallback: StateFlow<Boolean> = preferencesDataStore.autoFallbackFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun updateInput(text: String) {
        _uiState.update { it.copy(rawInput = text, errorMessage = null) }
    }

    fun selectStyle(style: PromptStyle) {
        _uiState.update { it.copy(selectedStyle = style) }
    }

    fun updateCustomRole(role: String) {
        _uiState.update { it.copy(customRole = role) }
    }

    fun updateCustomConstraints(constraints: String) {
        _uiState.update { it.copy(customConstraints = constraints) }
    }

    fun toggleAdvanced() {
        _uiState.update { it.copy(isAdvancedExpanded = !it.isAdvancedExpanded) }
    }

    fun clearInput() {
        _uiState.update {
            it.copy(
                rawInput = "",
                customRole = "",
                customConstraints = "",
                errorMessage = null
            )
        }
    }

    fun clearGeneratedPrompt() {
        _uiState.update {
            it.copy(
                generatedPrompt = null,
                generationResult = null,
                isSaved = false,
                errorMessage = null
            )
        }
    }

    fun resetAll() {
        _uiState.update {
            GeneratorUiState(
                selectedStyle = it.selectedStyle
            )
        }
    }

    fun applySample(text: String, style: PromptStyle, role: String = "", constraints: String = "") {
        _uiState.update {
            it.copy(
                rawInput = text,
                selectedStyle = style,
                customRole = role,
                customConstraints = constraints,
                errorMessage = null
            )
        }
    }

    fun generatePrompt() {
        val currentInput = _uiState.value.rawInput.trim()
        if (currentInput.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter some text or ideas to generate a prompt.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val slots = preferencesDataStore.apiSlotsFlow.first()
            val autoFallback = preferencesDataStore.autoFallbackFlow.first()
            val temp = preferencesDataStore.temperatureFlow.first()

            val userInput = UserPromptInput(
                rawText = currentInput,
                style = _uiState.value.selectedStyle,
                customRole = _uiState.value.customRole.ifBlank { null },
                customConstraints = _uiState.value.customConstraints.ifBlank { null }
            )

            val result = generatePromptUseCase(
                input = userInput,
                slots = slots,
                autoFallback = autoFallback,
                temperature = temp
            )

            result.fold(
                onSuccess = { genResult ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            generatedPrompt = genResult.prompt,
                            generationResult = genResult,
                            isSaved = true,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "Failed to generate prompt. Please check your API keys and connection."
                        )
                    }
                }
            )
        }
    }

    fun saveCurrentPrompt() {
        val prompt = _uiState.value.generatedPrompt ?: return
        viewModelScope.launch {
            savePromptUseCase(prompt)
            _uiState.update { it.copy(isSaved = true) }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    companion object {
        fun provideFactory(
            generatePromptUseCase: GeneratePromptUseCase,
            savePromptUseCase: SavePromptUseCase,
            preferencesDataStore: UserPreferencesDataStore
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return GeneratorViewModel(
                    generatePromptUseCase = generatePromptUseCase,
                    savePromptUseCase = savePromptUseCase,
                    preferencesDataStore = preferencesDataStore
                ) as T
            }
        }
    }
}
