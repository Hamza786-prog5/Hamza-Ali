package com.example.data.model

import java.util.UUID

enum class SenderType {
    USER,
    ASSISTANT
}

data class WikipediaArticle(
    val title: String,
    val summary: String,
    val url: String,
    val score: Int = 0
)

data class ResearchResult(
    val originalQuestion: String,
    val improvedQuestion: String,
    val answer: String,
    val sources: List<WikipediaArticle>,
    val relatedTopics: List<String>,
    val success: Boolean = true,
    val errorMessage: String? = null
)

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: SenderType,
    val content: String,
    val improvedQuestion: String? = null,
    val sources: List<WikipediaArticle> = emptyList(),
    val relatedTopics: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val isLoading: Boolean = false
)

data class OnlineUser(
    val userId: String,
    val deviceModel: String,
    val isCurrentDevice: Boolean = false,
    val isOnline: Boolean = true,
    val connectedAt: Long = System.currentTimeMillis(),
    val lastActiveAt: Long = System.currentTimeMillis(),
    val queriesCount: Int = 0,
    val lastQuery: String? = null
)

data class DynamicRulesConfig(
    val replacements: Map<String, String> = mapOf(
        "what in ai" to "artificial intelligence",
        "what is ai" to "artificial intelligence",
        "what ai" to "artificial intelligence",
        "tell me about ai" to "artificial intelligence",
        "what in robotics" to "robotics",
        "what is robotic" to "robotics",
        "what robotic" to "robotics",
        "who einstein" to "Albert Einstein",
        "who was einstein" to "Albert Einstein",
        "what python" to "Python programming language",
        "python kya hai" to "Python programming language",
        "robotics kya hai" to "robotics",
        "ai kya hai" to "artificial intelligence"
    ),
    val bonusKeywords: Map<String, Int> = mapOf(
        "artificial intelligence" to 20,
        "robotics" to 20,
        "python" to 20,
        "solar panel" to 20
    ),
    val filterWordsRegex: String = "\\b(please|tell me|can you|could you|batao|kya hai)\\b"
)

data class AdminConfig(
    val adminPassword: String = "admin123",
    val wikipediaSearchUrl: String = "https://en.wikipedia.org/w/api.php",
    val wikipediaSummaryUrl: String = "https://en.wikipedia.org/api/rest_v1/page/summary/",
    val searchLimit: Int = 8,
    val customAiApiKey: String = "",
    val customAiEndpoint: String = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent",
    val customAiModel: String = "gemini-1.5-flash",
    val enableHybridAi: Boolean = false,
    val systemPrompt: String = "You are Hamza AI, a smart assistant created by Hamza. Deliver concise, factual, and helpful responses."
)
