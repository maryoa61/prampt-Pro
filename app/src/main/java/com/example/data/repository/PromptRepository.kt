package com.example.data.repository

import com.example.data.local.db.PromptDao
import com.example.data.local.db.PromptEntity
import com.example.data.remote.AiPromptDataSource
import com.example.domain.model.GeneratedPrompt
import com.example.domain.model.UserPromptInput
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

interface PromptRepository {
    suspend fun generatePrompt(
        input: UserPromptInput,
        modelOverride: String? = null,
        temperature: Float = 0.5f
    ): Result<GeneratedPrompt>

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

    override suspend fun generatePrompt(
        input: UserPromptInput,
        modelOverride: String?,
        temperature: Float
    ): Result<GeneratedPrompt> {
        val result = remoteDataSource.generateStructuredPrompt(
            input = input,
            modelOverride = modelOverride,
            temperature = temperature
        )

        return result.map { structuredPromptText ->
            val generated = GeneratedPrompt(
                inputText = input.rawText,
                promptText = structuredPromptText,
                style = input.style,
                timestamp = System.currentTimeMillis()
            )
            val savedId = promptDao.insertPrompt(PromptEntity.fromDomain(generated))
            generated.copy(id = savedId)
        }
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
