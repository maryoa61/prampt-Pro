package com.example.domain.usecase

import com.example.data.repository.PromptRepository
import com.example.domain.model.GeneratedPrompt
import kotlinx.coroutines.flow.Flow

class SavePromptUseCase(
    private val repository: PromptRepository
) {
    suspend operator fun invoke(prompt: GeneratedPrompt): Long {
        return repository.savePrompt(prompt)
    }
}

class GetPromptHistoryUseCase(
    private val repository: PromptRepository
) {
    operator fun invoke(query: String = ""): Flow<List<GeneratedPrompt>> {
        return if (query.isBlank()) {
            repository.getAllPrompts()
        } else {
            repository.searchPrompts(query)
        }
    }
}

class DeletePromptUseCase(
    private val repository: PromptRepository
) {
    suspend operator fun invoke(id: Long) {
        repository.deletePromptById(id)
    }
}

class ClearPromptHistoryUseCase(
    private val repository: PromptRepository
) {
    suspend operator fun invoke() {
        repository.clearAllPrompts()
    }
}

class ExportPromptHistoryUseCase(
    private val repository: PromptRepository
) {
    operator fun invoke(): Flow<String> {
        return repository.exportPromptsAsJson()
    }
}
