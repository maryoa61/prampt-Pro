package com.example.promptpro.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity that stores templates as JSON blobs for complex fields (slots, defaults, examples).
 * We keep JSON strings to avoid custom join tables and for forward-compatibility/versioning.
 */
@Entity(tableName = "prompt_templates")
data class PromptTemplateEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String? = null,
    val slotsJson: String,        // JSON array of slot names
    val defaultValuesJson: String, // JSON object
    val examplesJson: String,     // JSON array of {input, output}
    val version: String,
    val createdAt: Long = System.currentTimeMillis()
)
