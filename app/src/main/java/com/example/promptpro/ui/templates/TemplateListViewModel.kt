package com.example.promptpro.ui.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.promptpro.domain.model.PromptTemplate
import com.example.promptpro.domain.usecase.DeleteTemplateUseCase
import com.example.promptpro.domain.usecase.ObserveTemplatesUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TemplateListViewModel(
    private val observeTemplatesUseCase: ObserveTemplatesUseCase,
    private val deleteTemplateUseCase: DeleteTemplateUseCase
) : ViewModel() {

    val templates: StateFlow<List<PromptTemplate>> = observeTemplatesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteTemplate(id: String) {
        viewModelScope.launch { deleteTemplateUseCase(id) }
    }

    companion object {
        fun provideFactory(
            observeTemplatesUseCase: ObserveTemplatesUseCase,
            deleteTemplateUseCase: DeleteTemplateUseCase
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TemplateListViewModel(observeTemplatesUseCase, deleteTemplateUseCase) as T
            }
        }
    }
}