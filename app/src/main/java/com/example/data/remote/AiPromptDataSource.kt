package com.example.data.remote

import com.example.BuildConfig
import com.example.domain.model.PromptStyle
import com.example.domain.model.UserPromptInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AiPromptDataSource(
    private val apiService: GeminiApiService,
    private val defaultModel: String = "gemini-2.5-flash"
) {

    suspend fun generateStructuredPrompt(
        input: UserPromptInput,
        modelOverride: String? = null,
        temperature: Float = 0.5f
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext Result.failure(
                    IllegalStateException(
                        "Gemini API key is not configured. Please ensure GEMINI_API_KEY is configured in AI Studio secrets."
                    )
                )
            }

            val systemInstructionText = """
                You are a world-renowned AI Prompt Engineer and LLM Architect.
                Your sole objective is to take raw, free-form, conversational, or unstructured input in ANY language (such as Persian / Farsi, Arabic, English, Spanish, mixed language, or shorthand notes) and transform it into a masterfully crafted, high-precision, production-grade prompt.
                
                CRITICAL MANDATES:
                1. MULTILINGUAL TRANSLATION & ADAPTATION: The user's input may be in Persian (فارسی), English, or any other language. You must deeply comprehend the semantic nuances and user intent, translate all concepts, and produce the prompt output EXCLUSIVELY in ENGLISH. Never include non-English words in the structured output.
                2. RIGID 5-SECTION STRUCTURE: You must format your final output strictly with these five uppercase headers in exact order:
                ROLE:
                CONTEXT:
                TASK:
                CONSTRAINTS:
                OUTPUT FORMAT:
                
                3. SECTION CONTENT GUIDELINES:
                   - ROLE: Define an elite, authoritative persona tailored to the domain.
                   - CONTEXT: Set up the background situation, problem landscape, and operational scope.
                   - TASK: Detail clear, step-by-step instructions of what the AI model must execute.
                   - CONSTRAINTS: Specify strict operational boundaries, prohibited actions, quality benchmarks, and edge cases.
                   - OUTPUT FORMAT: Specify exact structure, headers, syntax, markdown, or JSON templates expected.
                
                4. NO CHATTER: Do not wrap your response in markdown code blocks (no ``` or ```markdown). Do not provide conversational commentary, introductions, or sign-offs. Output ONLY the 5 sections with their content.
            """.trimIndent()

            val userMessageBuilder = StringBuilder()
            userMessageBuilder.append("TARGET DOMAIN / STYLE: ${input.style.displayName} (${input.style.domainFocus})\n\n")

            if (!input.customRole.isNullOrBlank()) {
                userMessageBuilder.append("USER SPECIFIED ROLE OVERRIDE: ${input.customRole.trim()}\n\n")
            }

            if (!input.customConstraints.isNullOrBlank()) {
                userMessageBuilder.append("USER SPECIFIED CONSTRAINTS: ${input.customConstraints.trim()}\n\n")
            }

            userMessageBuilder.append("RAW USER INPUT (ANY LANGUAGE):\n${input.rawText.trim()}\n\n")
            userMessageBuilder.append("Generate the complete, structured English prompt following the 5-section format now:")

            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        role = "user",
                        parts = listOf(GeminiPart(text = userMessageBuilder.toString()))
                    )
                ),
                systemInstruction = GeminiContent(
                    parts = listOf(GeminiPart(text = systemInstructionText))
                ),
                generationConfig = GeminiGenerationConfig(
                    temperature = temperature,
                    topP = 0.95f,
                    topK = 40,
                    maxOutputTokens = 2048
                )
            )

            val modelToUse = modelOverride?.ifBlank { defaultModel } ?: defaultModel
            val response = apiService.generateContent(
                model = modelToUse,
                apiKey = apiKey,
                request = request
            )

            if (response.error != null) {
                val errorMsg = response.error.message ?: "Gemini API error (code ${response.error.code})"
                return@withContext Result.failure(Exception(errorMsg))
            }

            val rawOutput = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (rawOutput.isNullOrBlank()) {
                return@withContext Result.failure(Exception("Gemini returned an empty response candidate."))
            }

            // Clean any unintentional enclosing markdown code fences
            val cleanedOutput = rawOutput
                .removePrefix("```markdown")
                .removePrefix("```text")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            Result.success(cleanedOutput)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
