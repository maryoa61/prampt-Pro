package com.example.promptpro.domain.model

data class PromptExample(
    val input: String,
    val output: String = ""
)

data class PromptTemplate(
    val id: String = "",
    val name: String,
    val description: String? = null,
    val slots: List<String> = emptyList(),
    val defaultValues: Map<String, String> = emptyMap(),
    val examples: List<PromptExample> = emptyList(),
    val version: String = "1.0",
    val createdAt: Long = System.currentTimeMillis()
)
