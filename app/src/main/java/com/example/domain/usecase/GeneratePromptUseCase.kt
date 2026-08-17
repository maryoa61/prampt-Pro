package com.example.domain.usecase

import com.example.data.repository.PromptRepository
import com.example.domain.model.GeneratedPrompt
import com.example.domain.model.UserPromptInput

class GeneratePromptUseCase(
    private val repository: PromptRepository
) {
    suspend operator fun invoke(
        input: UserPromptInput,
        modelOverride: String? = null,
        temperature: Float = 0.5f
    ): Result<GeneratedPrompt> {
        if (input.rawText.isBlank()) {
            return Result.failure(IllegalArgumentException("Prompt input cannot be empty."))
        }
        return repository.generatePrompt(input, modelOverride, temperature)
    }
}
