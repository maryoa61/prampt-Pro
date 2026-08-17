package com.example.data.local.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.GeneratedPrompt
import com.example.domain.model.PromptStyle

@Entity(tableName = "prompts_history")
data class PromptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "input_text")
    val inputText: String,
    @ColumnInfo(name = "prompt_text")
    val promptText: String,
    @ColumnInfo(name = "style")
    val style: String,
    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toDomain(): GeneratedPrompt {
        val promptStyle = try {
            PromptStyle.valueOf(style)
        } catch (_: Exception) {
            PromptStyle.GENERAL
        }
        return GeneratedPrompt(
            id = id,
            inputText = inputText,
            promptText = promptText,
            style = promptStyle,
            timestamp = timestamp
        )
    }

    companion object {
        fun fromDomain(domain: GeneratedPrompt): PromptEntity {
            return PromptEntity(
                id = domain.id,
                inputText = domain.inputText,
                promptText = domain.promptText,
                style = domain.style.name,
                timestamp = domain.timestamp
            )
        }
    }
}
