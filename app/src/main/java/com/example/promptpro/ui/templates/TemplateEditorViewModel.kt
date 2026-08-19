package com.example.promptpro.ui.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.promptpro.data.repo.PromptTemplateRepository
import com.example.promptpro.domain.model.PromptExample
import com.example.promptpro.domain.model.PromptTemplate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class TemplateEditorUiState(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val slots: List<String> = emptyList(),
    val defaultValues: Map<String, String> = emptyMap(),
    val examples: List<PromptExample> = emptyList(),
    val version: String = "1.0",
    val createdAt: Long = System.currentTimeMillis(),
    val loading: Boolean = false,
    val saving: Boolean = false,
    val error: String? = null
)

class TemplateEditorViewModel(
    private val repo: PromptTemplateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TemplateEditorUiState())
    val uiState: StateFlow<TemplateEditorUiState> = _uiState

    fun loadTemplate(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true)
            val t = repo.getById(id)
            _uiState.value = if (t != null) {
                _uiState.value.copy(
                    id = t.id,
                    name = t.name,
                    description = t.description ?: "",
                    slots = t.slots,
                    defaultValues = t.defaultValues,
                    examples = t.examples,
                    version = t.version,
                    createdAt = t.createdAt,
                    loading = false
                )
            } else {
                _uiState.value.copy(loading = false, error = "Template not found: $id")
            }
        }
    }

    fun onNameChange(v: String) { _uiState.value = _uiState.value.copy(name = v) }
    fun onDescriptionChange(v: String) { _uiState.value = _uiState.value.copy(description = v) }

    fun onAddSlot(slot: String) {
        val s = slot.trim()
        if (s.isBlank() || s in _uiState.value.slots) return
        _uiState.value = _uiState.value.copy(slots = _uiState.value.slots + s)
    }

    fun onRemoveSlot(slot: String) {
        _uiState.value = _uiState.value.copy(slots = _uiState.value.slots - slot)
    }

    fun onAddDefaultValue(key: String, value: String) {
        val k = key.trim()
        if (k.isBlank()) return
        _uiState.value = _uiState.value.copy(
            defaultValues = _uiState.value.defaultValues + (k to value.trim())
        )
    }

    fun onRemoveDefaultValue(key: String) {
        _uiState.value = _uiState.value.copy(defaultValues = _uiState.value.defaultValues - key)
    }

    fun onAddExample(input: String, output: String) {
        val i = input.trim()
        if (i.isBlank()) return
        _uiState.value = _uiState.value.copy(
            examples = _uiState.value.examples + PromptExample(i, output.trim())
        )
    }

    fun onRemoveExample(example: PromptExample) {
        _uiState.value = _uiState.value.copy(examples = _uiState.value.examples - example)
    }

    fun saveTemplate() {
        val name = _uiState.value.name.trim()
        if (name.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Template name is required.")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(saving = true, error = null)
            try {
                val t = PromptTemplate(
                    id = _uiState.value.id,
                    name = name,
                    description = _uiState.value.description.ifBlank { null },
                    slots = _uiState.value.slots,
                    defaultValues = _uiState.value.defaultValues,
                    examples = _uiState.value.examples,
                    version = _uiState.value.version,
                    createdAt = _uiState.value.createdAt
                )
                repo.upsert(t)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            } finally {
                _uiState.value = _uiState.value.copy(saving = false)
            }
        }
    }

    companion object {
        fun provideFactory(repo: PromptTemplateRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TemplateEditorViewModel(repo) as T
                }
            }
    }
}