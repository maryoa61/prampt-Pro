package com.example.domain.usecase

import com.example.data.repository.PromptRepository
import com.example.domain.model.ApiKeySlot
import com.example.domain.model.GenerationResult
import com.example.domain.model.UserPromptInput

class GeneratePromptUseCase(
    private val repository: PromptRepository
) {
    suspend operator fun invoke(
        input: UserPromptInput,
        slots: List<ApiKeySlot>,
        autoFallback: Boolean = true,
        temperature: Float = 0.5f
    ): Result<GenerationResult> {
        if (input.rawText.isBlank()) {
            return Result.failure(IllegalArgumentException("Prompt input cannot be empty."))
        }
        return repository.generatePromptWithFallback(
            input = input,
            slots = slots,
            autoFallback = autoFallback,
            temperature = temperature
        )
    }
}
