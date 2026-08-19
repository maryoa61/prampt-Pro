package com.example.promptpro.data.mappers

import com.example.promptpro.data.local.PromptTemplateEntity
import com.example.promptpro.domain.model.PromptExample
import com.example.promptpro.domain.model.PromptTemplate
import org.json.JSONArray
import org.json.JSONObject

/**
 * Mappers between Room entity and domain model.
 *
 * JSON (de)serialization uses org.json (already a project dependency via
 * AiPromptDataSource) instead of kotlinx.serialization, so no new Gradle
 * plugin/dependency is required.
 */
fun PromptTemplateEntity.toDomain(): PromptTemplate = PromptTemplate(
    id = id,
    name = name,
    description = description,
    slots = decodeStringList(slotsJson),
    defaultValues = decodeStringMap(defaultValuesJson),
    examples = decodeExamples(examplesJson),
    version = version,
    createdAt = createdAt
)

fun PromptTemplate.toEntity(): PromptTemplateEntity = PromptTemplateEntity(
    id = id,
    name = name,
    description = description,
    slotsJson = encodeStringList(slots),
    defaultValuesJson = encodeStringMap(defaultValues),
    examplesJson = encodeExamples(examples),
    version = version,
    createdAt = createdAt
)

private fun encodeStringList(list: List<String>): String {
    val arr = JSONArray()
    list.forEach { arr.put(it) }
    return arr.toString()
}

private fun decodeStringList(json: String): List<String> = try {
    val arr = JSONArray(json)
    val out = mutableListOf<String>()
    for (i in 0 until arr.length()) out.add(arr.getString(i))
    out
} catch (_: Exception) {
    emptyList()
}

private fun encodeStringMap(map: Map<String, String>): String {
    val obj = JSONObject()
    map.forEach { (k, v) -> obj.put(k, v) }
    return obj.toString()
}

private fun decodeStringMap(json: String): Map<String, String> = try {
    val obj = JSONObject(json)
    val out = mutableMapOf<String, String>()
    val keys = obj.keys()
    while (keys.hasNext()) {
        val k = keys.next()
        out[k] = obj.getString(k)
    }
    out
} catch (_: Exception) {
    emptyMap()
}

private fun encodeExamples(examples: List<PromptExample>): String {
    val arr = JSONArray()
    examples.forEach { ex ->
        arr.put(JSONObject().put("input", ex.input).put("output", ex.output))
    }
    return arr.toString()
}

private fun decodeExamples(json: String): List<PromptExample> = try {
    val arr = JSONArray(json)
    val out = mutableListOf<PromptExample>()
    for (i in 0 until arr.length()) {
        val o = arr.getJSONObject(i)
        out.add(PromptExample(o.optString("input"), o.optString("output")))
    }
    out
} catch (_: Exception) {
    emptyList()
}
