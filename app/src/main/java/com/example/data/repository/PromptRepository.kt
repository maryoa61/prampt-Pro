package com.example.data.repository

import com.example.BuildConfig
import com.example.data.local.db.PromptDao
import com.example.data.local.db.PromptEntity
import com.example.data.remote.AiPromptDataSource
import com.example.domain.model.ApiKeySlot
import com.example.domain.model.ApiProvider
import com.example.domain.model.GeneratedPrompt
import com.example.domain.model.GenerationResult
import com.example.domain.model.UserPromptInput
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

interface PromptRepository {
    suspend fun generatePromptWithFallback(
        input: UserPromptInput,
        slots: List<ApiKeySlot>,
        autoFallback: Boolean = true,
        temperature: Float = 0.5f
    ): Result<GenerationResult>

    suspend fun savePrompt(prompt: GeneratedPrompt): Long
    fun getAllPrompts(): Flow<List<GeneratedPrompt>>
    fun searchPrompts(query: String): Flow<List<GeneratedPrompt>>
    suspend fun deletePromptById(id: Long)
    suspend fun clearAllPrompts()
    fun exportPromptsAsJson(): Flow<String>
}

class PromptRepositoryImpl(
    private val remoteDataSource: AiPromptDataSource,
    private val promptDao: PromptDao
) : PromptRepository {

    override suspend fun generatePromptWithFallback(
        input: UserPromptInput,
        slots: List<ApiKeySlot>,
        autoFallback: Boolean,
        temperature: Float
    ): Result<GenerationResult> {
        val activeSlots = slots.filter { it.isEnabled }
        if (activeSlots.isEmpty()) {
            return Result.failure(
                IllegalStateException("No API slot is enabled. Please configure and enable at least one API slot in Settings.")
            )
        }

        val slotsToTry = if (autoFallback) activeSlots else listOf(activeSlots.first())
        val errors = mutableListOf<String>()

        for ((index, slot) in slotsToTry.withIndex()) {
            val key = getEffectiveApiKey(slot)
            
            // If slot has no key and isn't Gemini with BuildConfig, record and continue if fallback enabled
            if (key.isBlank() && !(slot.provider == ApiProvider.GEMINI && BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY")) {
                val errMsg = "${slot.label} (${slot.provider.displayName}): API key is empty/not configured."
                errors.add(errMsg)
                if (!autoFallback) {
                    return Result.failure(IllegalStateException(errMsg))
                }
                continue
            }

            val result = remoteDataSource.generateStructuredPrompt(
                input = input,
                provider = slot.provider,
                customApiKey = key,
                customEndpoint = slot.customEndpoint.ifBlank { null },
                modelOverride = slot.model.ifBlank { null },
                temperature = temperature
            )

            if (result.isSuccess) {
                val structuredPromptText = result.getOrThrow()
                val generated = GeneratedPrompt(
                    inputText = input.rawText,
                    promptText = structuredPromptText,
                    style = input.style,
                    timestamp = System.currentTimeMillis()
                )
                val savedId = promptDao.insertPrompt(PromptEntity.fromDomain(generated))
                val finalPrompt = generated.copy(id = savedId)

                return Result.success(
                    GenerationResult(
                        prompt = finalPrompt,
                        usedSlot = slot,
                        attemptedSlotsCount = index + 1,
                        fallbackOccurred = index > 0,
                        fallbackHistory = errors.toList()
                    )
                )
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Unknown error"
                errors.add("${slot.label} (${slot.provider.displayName}): $errorMsg")
                
                if (!autoFallback) {
                    return Result.failure(result.exceptionOrNull() ?: Exception(errorMsg))
                }
            }
        }

        // All slots failed or exhausted
        val combinedErrors = errors.joinToString("\n\n")
        return Result.failure(
            Exception("All available API slots failed or exhausted credit/quota:\n$combinedErrors")
        )
    }

    private fun getEffectiveApiKey(slot: ApiKeySlot): String {
        if (slot.apiKey.isNotBlank()) return slot.apiKey.trim()
        if (slot.provider == ApiProvider.GEMINI) {
            val buildKey = BuildConfig.GEMINI_API_KEY
            if (buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY") return buildKey
        }
        return ""
    }

    override suspend fun savePrompt(prompt: GeneratedPrompt): Long {
        return promptDao.insertPrompt(PromptEntity.fromDomain(prompt))
    }

    override fun getAllPrompts(): Flow<List<GeneratedPrompt>> {
        return promptDao.getAllPrompts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun searchPrompts(query: String): Flow<List<GeneratedPrompt>> {
        return if (query.isBlank()) {
            getAllPrompts()
        } else {
            promptDao.searchPrompts(query.trim()).map { entities ->
                entities.map { it.toDomain() }
            }
        }
    }

    override suspend fun deletePromptById(id: Long) {
        promptDao.deletePromptById(id)
    }

    override suspend fun clearAllPrompts() {
        promptDao.clearAll()
    }

    override fun exportPromptsAsJson(): Flow<String> {
        return getAllPrompts().map { prompts ->
            val jsonArray = JSONArray()
            prompts.forEach { item ->
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("inputText", item.inputText)
                    put("promptText", item.promptText)
                    put("style", item.style.name)
                    put("timestamp", item.timestamp)
                }
                jsonArray.put(obj)
            }
            jsonArray.toString(2)
        }
    }
}
