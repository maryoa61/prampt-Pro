package com.example.domain.model

enum class PromptStyle(
    val displayName: String,
    val persianName: String,
    val description: String,
    val domainFocus: String
) {
    SOFTWARE_DEVELOPMENT(
        displayName = "Software Development",
        persianName = "توسعه نرم‌افزار",
        description = "Architecture, clean code, debugging, APIs, and tests",
        domainFocus = "Software engineering, system architecture, API design, clean code practices, security, and edge-case testing."
    ),
    CREATIVE_WRITING(
        displayName = "Creative Writing",
        persianName = "نویسندگی خلاق",
        description = "Storytelling, world-building, narrative arcs, and dialogue",
        domainFocus = "Narrative design, character motivation, vivid imagery, evocative prose, and storytelling structure."
    ),
    BUSINESS_MARKETING(
        displayName = "Business & Marketing",
        persianName = "کسب‌وکار و بازاریابی",
        description = "Pitches, marketing campaigns, copywriting, and strategy",
        domainFocus = "Conversion-focused copywriting, ROI analysis, market positioning, target persona engagement, and compelling calls-to-action."
    ),
    DATA_ANALYSIS(
        displayName = "Data & Research",
        persianName = "داده و تحلیل",
        description = "SQL, Python data science, statistics, and methodology",
        domainFocus = "Data processing pipelines, SQL optimization, statistical rigor, insight extraction, and data visualization guidelines."
    ),
    GENERAL(
        displayName = "General Purpose",
        persianName = "همه‌منظوره و عمومی",
        description = "Clear, versatile, and balanced multi-purpose prompt",
        domainFocus = "Comprehensive reasoning, balanced clarity, structured breakdown, and versatile actionable guidance."
    )
}

data class UserPromptInput(
    val rawText: String,
    val style: PromptStyle = PromptStyle.GENERAL,
    val customRole: String? = null,
    val customConstraints: String? = null
)

data class GeneratedPrompt(
    val id: Long = 0,
    val inputText: String,
    val promptText: String,
    val style: PromptStyle,
    val timestamp: Long = System.currentTimeMillis()
)

data class StructuredPromptSections(
    val role: String,
    val context: String,
    val task: String,
    val constraints: String,
    val outputFormat: String,
    val rawText: String
) {
    companion object {
        fun parse(rawText: String): StructuredPromptSections {
            fun extractSection(header: String, nextHeaders: List<String>): String {
                val regex = Regex("""(?i)(?:^|\n)${Regex.escape(header)}:\s*([\s\S]*?)(?=(?:\n(?:${nextHeaders.joinToString("|") { Regex.escape(it) }}):)|\z)""")
                val match = regex.find(rawText)
                return match?.groupValues?.get(1)?.trim() ?: ""
            }

            val role = extractSection("ROLE", listOf("CONTEXT", "TASK", "CONSTRAINTS", "OUTPUT FORMAT"))
            val context = extractSection("CONTEXT", listOf("TASK", "CONSTRAINTS", "OUTPUT FORMAT"))
            val task = extractSection("TASK", listOf("CONSTRAINTS", "OUTPUT FORMAT"))
            val constraints = extractSection("CONSTRAINTS", listOf("OUTPUT FORMAT"))
            val outputFormat = extractSection("OUTPUT FORMAT", emptyList())

            return StructuredPromptSections(
                role = role.ifBlank { "Expert AI Assistant in this domain" },
                context = context,
                task = task.ifBlank { rawText },
                constraints = constraints,
                outputFormat = outputFormat,
                rawText = rawText
            )
        }
    }
}
