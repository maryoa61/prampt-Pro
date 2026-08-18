package com.example.data.remote

import com.example.BuildConfig
import com.example.domain.model.ApiProvider
import com.example.domain.model.HeaderAuthType
import com.example.domain.model.PromptStyle
import com.example.domain.model.UserPromptInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class AiPromptDataSource(
    private val apiService: GeminiApiService,
    private val okHttpClient: OkHttpClient,
    private val defaultModel: String = "gemini-2.5-flash"
) {

    private val systemInstructionText = """
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

    suspend fun generateStructuredPrompt(
        input: UserPromptInput,
        provider: ApiProvider = ApiProvider.GEMINI,
        customApiKey: String? = null,
        customEndpoint: String? = null,
        modelOverride: String? = null,
        temperature: Float = 0.5f
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val userMessage = buildUserPromptText(input)

            when (provider) {
                ApiProvider.GEMINI -> {
                    val keyToUse = if (!customApiKey.isNullOrBlank()) {
                        customApiKey.trim()
                    } else {
                        val buildKey = BuildConfig.GEMINI_API_KEY
                        if (buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY") buildKey else ""
                    }

                    if (keyToUse.isBlank()) {
                        return@withContext Result.failure(
                            IllegalStateException(
                                "Gemini API key is not configured. Please enter your API Key in Settings or configure GEMINI_API_KEY in AI Studio secrets."
                            )
                        )
                    }

                    val request = GeminiRequest(
                        contents = listOf(
                            GeminiContent(
                                role = "user",
                                parts = listOf(GeminiPart(text = userMessage))
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

                    val modelToUse = modelOverride?.ifBlank { provider.defaultModel } ?: provider.defaultModel
                    val response = apiService.generateContent(
                        model = modelToUse,
                        apiKey = keyToUse,
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

                    Result.success(cleanOutput(rawOutput))
                }

                ApiProvider.CLAUDE -> {
                    val keyToUse = customApiKey?.trim() ?: ""
                    if (keyToUse.isBlank()) {
                        return@withContext Result.failure(
                            IllegalStateException("Anthropic Claude API key is required. Please set your Claude API key in Settings.")
                        )
                    }
                    generateClaudePrompt(
                        userMessage = userMessage,
                        apiKey = keyToUse,
                        model = modelOverride?.ifBlank { provider.defaultModel } ?: provider.defaultModel,
                        endpoint = if (!customEndpoint.isNullOrBlank()) customEndpoint.trim() else provider.defaultEndpoint,
                        temperature = temperature
                    )
                }

                ApiProvider.OPENAI,
                ApiProvider.DEEPSEEK,
                ApiProvider.NVIDIA,
                ApiProvider.GROQ,
                ApiProvider.CUSTOM_OPENAI_COMPATIBLE -> {
                    val keyToUse = customApiKey?.trim() ?: ""
                    if (keyToUse.isBlank()) {
                        return@withContext Result.failure(
                            IllegalStateException("${provider.displayName} API key is required. Please set your API key in Settings.")
                        )
                    }

                    val endpoint = if (!customEndpoint.isNullOrBlank()) {
                        customEndpoint.trim()
                    } else {
                        provider.defaultEndpoint
                    }

                    val model = modelOverride?.ifBlank { provider.defaultModel } ?: provider.defaultModel

                    generateOpenAiCompatiblePrompt(
                        userMessage = userMessage,
                        apiKey = keyToUse,
                        endpoint = endpoint,
                        model = model,
                        temperature = temperature,
                        provider = provider
                    )
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildUserPromptText(input: UserPromptInput): String {
        val builder = StringBuilder()
        builder.append("TARGET DOMAIN / STYLE: ${input.style.displayName} (${input.style.domainFocus})\n\n")

        if (!input.customRole.isNullOrBlank()) {
            builder.append("USER SPECIFIED ROLE OVERRIDE: ${input.customRole.trim()}\n\n")
        }

        if (!input.customConstraints.isNullOrBlank()) {
            builder.append("USER SPECIFIED CONSTRAINTS: ${input.customConstraints.trim()}\n\n")
        }

        builder.append("RAW USER INPUT (ANY LANGUAGE):\n${input.rawText.trim()}\n\n")
        builder.append("Generate the complete, structured English prompt following the 5-section format now:")
        return builder.toString()
    }

    private fun generateOpenAiCompatiblePrompt(
        userMessage: String,
        apiKey: String,
        endpoint: String,
        model: String,
        temperature: Float,
        provider: ApiProvider
    ): Result<String> {
        try {
            val jsonBody = JSONObject().apply {
                put("model", model)
                put("temperature", temperature)
                put("max_tokens", 2048)

                val messages = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", systemInstructionText)
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", userMessage)
                    })
                }
                put("messages", messages)
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val requestBuilder = Request.Builder()
                .url(endpoint)
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer $apiKey")

            val response = okHttpClient.newCall(requestBuilder.build()).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorMsg = try {
                    val errJson = JSONObject(responseBody)
                    errJson.optJSONObject("error")?.optString("message") ?: responseBody
                } catch (e: Exception) {
                    "HTTP ${response.code}: $responseBody"
                }
                return Result.failure(Exception("${provider.displayName} API error: $errorMsg"))
            }

            val jsonResponse = JSONObject(responseBody)
            val choices = jsonResponse.optJSONArray("choices")
            if (choices == null || choices.length() == 0) {
                return Result.failure(Exception("${provider.displayName} returned no choices in response."))
            }

            val content = choices.getJSONObject(0).optJSONObject("message")?.optString("content")
            if (content.isNullOrBlank()) {
                return Result.failure(Exception("${provider.displayName} returned empty message content."))
            }

            return Result.success(cleanOutput(content))
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    private fun generateClaudePrompt(
        userMessage: String,
        apiKey: String,
        model: String,
        endpoint: String,
        temperature: Float
    ): Result<String> {
        try {
            val jsonBody = JSONObject().apply {
                put("model", model)
                put("max_tokens", 2048)
                put("temperature", temperature)
                put("system", systemInstructionText)

                val messages = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", userMessage)
                    })
                }
                put("messages", messages)
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(endpoint)
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .addHeader("x-api-key", apiKey)
                .addHeader("anthropic-version", "2023-06-01")
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorMsg = try {
                    val errJson = JSONObject(responseBody)
                    errJson.optJSONObject("error")?.optString("message") ?: responseBody
                } catch (e: Exception) {
                    "HTTP ${response.code}: $responseBody"
                }
                return Result.failure(Exception("Claude API error: $errorMsg"))
            }

            val jsonResponse = JSONObject(responseBody)
            val contents = jsonResponse.optJSONArray("content")
            if (contents == null || contents.length() == 0) {
                return Result.failure(Exception("Claude returned no content blocks."))
            }

            val contentText = contents.getJSONObject(0).optString("text")
            if (contentText.isBlank()) {
                return Result.failure(Exception("Claude returned empty text in response."))
            }

            return Result.success(cleanOutput(contentText))
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    private fun cleanOutput(raw: String): String {
        return raw
            .removePrefix("```markdown")
            .removePrefix("```text")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
    }
}
