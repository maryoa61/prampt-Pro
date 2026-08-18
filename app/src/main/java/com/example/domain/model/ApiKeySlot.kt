package com.example.domain.model

data class ApiKeySlot(
    val slotIndex: Int, // 0 = Primary, 1 = Backup 1, 2 = Backup 2, 3 = Backup 3
    val label: String,
    val provider: ApiProvider = ApiProvider.GEMINI,
    val apiKey: String = "",
    val model: String = ApiProvider.GEMINI.defaultModel,
    val customEndpoint: String = "",
    val isEnabled: Boolean = true
) {
    val isPrimary: Boolean get() = slotIndex == 0
    val hasKey: Boolean get() = apiKey.isNotBlank()
}
