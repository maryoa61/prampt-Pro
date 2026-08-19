package com.example.promptpro.data.repo

import com.example.promptpro.data.local.PromptTemplateDao
import com.example.promptpro.data.mappers.toDomain
import com.example.promptpro.data.mappers.toEntity
import com.example.promptpro.domain.model.PromptTemplate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class PromptTemplateRepository(
    private val dao: PromptTemplateDao
) {
    fun observeAll(): Flow<List<PromptTemplate>> =
        dao.observeAllTemplates().map { list -> list.map { it.toDomain() } }

    suspend fun getById(id: String): PromptTemplate? =
        dao.getById(id)?.toDomain()

    suspend fun upsert(template: PromptTemplate) {
        val t = if (template.id.isBlank()) {
            template.copy(id = UUID.randomUUID().toString())
        } else {
            template
        }
        dao.upsert(t.toEntity())
    }

    suspend fun delete(id: String) = dao.delete(id)
}
