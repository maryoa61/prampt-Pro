package com.example.domain.model

enum class ApiProvider(
    val id: String,
    val displayName: String,
    val defaultEndpoint: String,
    val defaultModel: String,
    val availableModels: List<String>,
    val headerAuthType: HeaderAuthType
) {
    GEMINI(
        id = "gemini",
        displayName = "Google Gemini",
        defaultEndpoint = "https://generativelanguage.googleapis.com/v1beta/models/",
        defaultModel = "gemini-2.5-flash",
        availableModels = listOf("gemini-2.5-flash", "gemini-3.5-flash", "gemini-3.1-pro-preview"),
        headerAuthType = HeaderAuthType.QUERY_OR_HEADER
    ),
    OPENAI(
        id = "openai",
        displayName = "OpenAI (ChatGPT)",
        defaultEndpoint = "https://api.openai.com/v1/chat/completions",
        defaultModel = "gpt-4o-mini",
        availableModels = listOf("gpt-4o-mini", "gpt-4o", "gpt-4-turbo", "o1-mini"),
        headerAuthType = HeaderAuthType.BEARER
    ),
    CLAUDE(
        id = "claude",
        displayName = "Anthropic Claude",
        defaultEndpoint = "https://api.anthropic.com/v1/messages",
        defaultModel = "claude-3-5-sonnet-20241022",
        availableModels = listOf("claude-3-5-sonnet-20241022", "claude-3-5-haiku-20241022", "claude-3-opus-20240229"),
        headerAuthType = HeaderAuthType.X_API_KEY
    ),
    DEEPSEEK(
        id = "deepseek",
        displayName = "DeepSeek",
        defaultEndpoint = "https://api.deepseek.com/v1/chat/completions",
        defaultModel = "deepseek-chat",
        availableModels = listOf("deepseek-chat", "deepseek-reasoner"),
        headerAuthType = HeaderAuthType.BEARER
    ),
    NVIDIA(
        id = "nvidia",
        displayName = "NVIDIA NIM",
        defaultEndpoint = "https://integrate.api.nvidia.com/v1/chat/completions",
        defaultModel = "meta/llama-3.1-70b-instruct",
        availableModels = listOf("meta/llama-3.1-70b-instruct", "meta/llama-3.1-405b-instruct", "mistralai/mixtral-8x22b-instruct-v0.1"),
        headerAuthType = HeaderAuthType.BEARER
    ),
    GROQ(
        id = "groq",
        displayName = "Groq",
        defaultEndpoint = "https://api.groq.com/openai/v1/chat/completions",
        defaultModel = "llama-3.3-70b-versatile",
        availableModels = listOf("llama-3.3-70b-versatile", "llama-3.1-8b-instant", "mixtral-8x7b-32768"),
        headerAuthType = HeaderAuthType.BEARER
    ),
    CUSTOM_OPENAI_COMPATIBLE(
        id = "custom_openai",
        displayName = "Custom (OpenAI Compatible)",
        defaultEndpoint = "https://api.openai.com/v1/chat/completions",
        defaultModel = "default",
        availableModels = listOf("default", "custom-model"),
        headerAuthType = HeaderAuthType.BEARER
    );

    companion object {
        fun fromId(id: String): ApiProvider {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: GEMINI
        }
    }
}

enum class HeaderAuthType {
    BEARER,
    X_API_KEY,
    QUERY_OR_HEADER
}
