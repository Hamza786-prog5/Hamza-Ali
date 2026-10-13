package com.example.engine

import com.example.data.model.AdminConfig
import com.example.data.model.DynamicRulesConfig
import com.example.data.model.ResearchResult
import com.example.data.model.WikipediaArticle
import com.example.data.repository.WikipediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class HamzaAiResearchEngine(
    private val wikipediaRepository: WikipediaRepository
) {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    fun improveQuestion(
        rawQuestion: String,
        rules: DynamicRulesConfig
    ): String {
        val q = rawQuestion.trim()
        val low = q.lowercase()

        // 1. Check direct replacement mapping
        rules.replacements[low]?.let { return it }

        // 2. Strip filler conversational words
        var cleaned = q
        try {
            if (rules.filterWordsRegex.isNotBlank()) {
                val regex = Regex(rules.filterWordsRegex, RegexOption.IGNORE_CASE)
                cleaned = regex.replace(cleaned, "")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 3. Normalize whitespace
        cleaned = cleaned.replace(Regex("\\s+"), " ").trim()
        return cleaned.ifBlank { rawQuestion.trim() }
    }

    fun scoreResult(
        title: String,
        summary: String,
        question: String,
        bonusKeywords: Map<String, Int>
    ): Int {
        val titleLow = title.lowercase()
        val summaryLow = summary.lowercase()
        val questionLow = question.lowercase()

        var score = 0

        val words = questionLow.split(Regex("\\s+"))
        for (w in words) {
            val cleanedWord = w.replace(Regex("[^a-zA-Z0-9]"), "")
            if (cleanedWord.length > 2) {
                if (titleLow.contains(cleanedWord)) {
                    score += 5
                }
                if (summaryLow.contains(cleanedWord)) {
                    score += 1
                }
            }
        }

        // Dynamic bonus keyword score matching
        for ((keyword, bonus) in bonusKeywords) {
            val kwLow = keyword.lowercase()
            if (questionLow.contains(kwLow) && titleLow.contains(kwLow)) {
                score += bonus
            }
        }

        return score
    }

    suspend fun research(
        rawQuestion: String,
        config: AdminConfig,
        rules: DynamicRulesConfig
    ): ResearchResult = withContext(Dispatchers.IO) {
        val improved = improveQuestion(rawQuestion, rules)

        val searchTitles = wikipediaRepository.searchWikipedia(
            baseUrl = config.wikipediaSearchUrl,
            query = improved,
            limit = config.searchLimit
        )

        val articles = mutableListOf<WikipediaArticle>()
        for (title in searchTitles) {
            if (title.isBlank()) continue
            val article = wikipediaRepository.fetchSummary(
                baseUrl = config.wikipediaSummaryUrl,
                title = title
            )
            if (article != null && article.summary.isNotBlank()) {
                val score = scoreResult(
                    title = article.title,
                    summary = article.summary,
                    question = improved,
                    bonusKeywords = rules.bonusKeywords
                )
                articles.add(article.copy(score = score))
            }
        }

        articles.sortByDescending { it.score }

        if (articles.isEmpty()) {
            return@withContext ResearchResult(
                originalQuestion = rawQuestion,
                improvedQuestion = improved,
                answer = "I could not find enough information for this question from internet knowledge sources.\n\nTry rephrasing your question or checking network connectivity.",
                sources = emptyList(),
                relatedTopics = emptyList(),
                success = false
            )
        }

        val main = articles.first()
        val title = main.title
        val summary = main.summary

        // Extract first 4 sentences
        val sentences = summary.split(Regex("(?<=[.!?])\\s+"))
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val mainSentencesText = sentences.take(4).joinToString(" ")

        // Extract related topics
        val relatedTopics = mutableListOf<String>()
        for (item in articles.drop(1)) {
            if (item.title != title && !relatedTopics.contains(item.title)) {
                relatedTopics.add(item.title)
                if (relatedTopics.size >= 4) break
            }
        }

        // Check if Hybrid AI is enabled with custom API key
        var answer = if (config.enableHybridAi && config.customAiApiKey.isNotBlank()) {
            callHybridAi(rawQuestion, improved, mainSentencesText, relatedTopics, config)
                ?: formatStandardAnswer(title, mainSentencesText, relatedTopics)
        } else {
            formatStandardAnswer(title, mainSentencesText, relatedTopics)
        }

        ResearchResult(
            originalQuestion = rawQuestion,
            improvedQuestion = improved,
            answer = answer,
            sources = articles.take(3),
            relatedTopics = relatedTopics,
            success = true
        )
    }

    private fun formatStandardAnswer(
        title: String,
        summarySentences: String,
        relatedTopics: List<String>
    ): String {
        val answerBuilder = StringBuilder()
        answerBuilder.append("Based on my internet research:\n\n")
        answerBuilder.append("$title:\n\n")
        answerBuilder.append(summarySentences)

        if (relatedTopics.isNotEmpty()) {
            answerBuilder.append("\n\nRelated topics:")
            for (topic in relatedTopics) {
                answerBuilder.append("\n• $topic")
            }
        }

        return answerBuilder.toString()
    }

    private suspend fun callHybridAi(
        question: String,
        improved: String,
        factsSummary: String,
        relatedTopics: List<String>,
        config: AdminConfig
    ): String? = withContext(Dispatchers.IO) {
        try {
            val endpoint = if (config.customAiEndpoint.contains("generativelanguage.googleapis.com")) {
                val base = config.customAiEndpoint.substringBefore("?")
                "$base?key=${config.customAiApiKey}"
            } else {
                config.customAiEndpoint
            }

            val prompt = """
                ${config.systemPrompt}
                User Question: $question (Improved: $improved)
                Internet Verified Knowledge: $factsSummary
                Related Topics: ${relatedTopics.joinToString(", ")}
                
                Please provide a clear, accurate, and insightful answer integrating the verified knowledge.
            """.trimIndent()

            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", prompt))
                        })
                    })
                })
            }

            val request = Request.Builder()
                .url(endpoint)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val respStr = response.body?.string() ?: return@withContext null
                val root = JSONObject(respStr)
                val candidates = root.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val first = candidates.getJSONObject(0)
                    val content = first.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).optString("text", null)
                    }
                }
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
