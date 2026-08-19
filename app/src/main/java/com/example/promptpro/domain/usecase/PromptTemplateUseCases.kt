package com.example.promptpro.domain.usecase

import com.example.promptpro.data.repo.PromptTemplateRepository
import com.example.promptpro.domain.model.PromptTemplate
import kotlinx.coroutines.flow.Flow

class ObserveTemplatesUseCase(
    private val repository: PromptTemplateRepository
) {
    operator fun invoke(): Flow<List<PromptTemplate>> = repository.observeAll()
}

class GetTemplateByIdUseCase(
    private val repository: PromptTemplateRepository
) {
    suspend operator fun invoke(id: String): PromptTemplate? = repository.getById(id)
}

class SaveTemplateUseCase(
    private val repository: PromptTemplateRepository
) {
    suspend operator fun invoke(template: PromptTemplate) = repository.upsert(template)
}

class DeleteTemplateUseCase(
    private val repository: PromptTemplateRepository
) {
    suspend operator fun invoke(id: String) = repository.delete(id)
}
