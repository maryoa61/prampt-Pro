package com.example.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.model.GeneratedPrompt
import com.example.domain.model.PromptStyle
import com.example.domain.usecase.ClearPromptHistoryUseCase
import com.example.domain.usecase.DeletePromptUseCase
import com.example.domain.usecase.ExportPromptHistoryUseCase
import com.example.domain.usecase.GetPromptHistoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistoryUiState(
    val searchQuery: String = "",
    val filterStyle: PromptStyle? = null,
    val prompts: List<GeneratedPrompt> = emptyList(),
    val isDeletingAll: Boolean = false,
    val exportedJson: String? = null
)

class HistoryViewModel(
    private val getPromptHistoryUseCase: GetPromptHistoryUseCase,
    private val deletePromptUseCase: DeletePromptUseCase,
    private val clearPromptHistoryUseCase: ClearPromptHistoryUseCase,
    private val exportPromptHistoryUseCase: ExportPromptHistoryUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterStyle = MutableStateFlow<PromptStyle?>(null)
    val filterStyle: StateFlow<PromptStyle?> = _filterStyle.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val prompts: StateFlow<List<GeneratedPrompt>> = _searchQuery
        .flatMapLatest { query ->
            getPromptHistoryUseCase(query)
        }
        .combine(_filterStyle) { list, styleFilter ->
            if (styleFilter == null) list else list.filter { it.style == styleFilter }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterStyle(style: PromptStyle?) {
        _filterStyle.value = style
    }

    fun deletePrompt(id: Long) {
        viewModelScope.launch {
            deletePromptUseCase(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            clearPromptHistoryUseCase()
        }
    }

    companion object {
        fun provideFactory(
            getPromptHistoryUseCase: GetPromptHistoryUseCase,
            deletePromptUseCase: DeletePromptUseCase,
            clearPromptHistoryUseCase: ClearPromptHistoryUseCase,
            exportPromptHistoryUseCase: ExportPromptHistoryUseCase
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HistoryViewModel(
                    getPromptHistoryUseCase = getPromptHistoryUseCase,
                    deletePromptUseCase = deletePromptUseCase,
                    clearPromptHistoryUseCase = clearPromptHistoryUseCase,
                    exportPromptHistoryUseCase = exportPromptHistoryUseCase
                ) as T
            }
        }
    }
}
