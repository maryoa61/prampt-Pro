package com.example.domain.model

data class GenerationResult(
    val prompt: GeneratedPrompt,
    val usedSlot: ApiKeySlot,
    val attemptedSlotsCount: Int = 1,
    val fallbackOccurred: Boolean = false,
    val fallbackHistory: List<String> = emptyList()
)
