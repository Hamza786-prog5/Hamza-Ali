package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.AdminConfig
import com.example.data.model.DynamicRulesConfig
import com.example.data.model.OnlineUser
import org.json.JSONObject
import java.util.UUID

class AdminConfigRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("hamza_ai_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ADMIN_PASSWORD = "admin_password"
        private const val KEY_WIKI_SEARCH_URL = "wiki_search_url"
        private const val KEY_WIKI_SUMMARY_URL = "wiki_summary_url"
        private const val KEY_SEARCH_LIMIT = "search_limit"
        private const val KEY_CUSTOM_AI_KEY = "custom_ai_key"
        private const val KEY_CUSTOM_AI_ENDPOINT = "custom_ai_endpoint"
        private const val KEY_CUSTOM_AI_MODEL = "custom_ai_model"
        private const val KEY_ENABLE_HYBRID_AI = "enable_hybrid_ai"
        private const val KEY_SYSTEM_PROMPT = "system_prompt"
        private const val KEY_RULES_CODE = "rules_code"
        private const val KEY_CURRENT_USER_ID = "current_user_id"
    }

    private val defaultRulesJson = """
{
  "replacements": {
    "what in ai": "artificial intelligence",
    "what is ai": "artificial intelligence",
    "what ai": "artificial intelligence",
    "tell me about ai": "artificial intelligence",
    "what in robotics": "robotics",
    "what is robotic": "robotics",
    "what robotic": "robotics",
    "who einstein": "Albert Einstein",
    "who was einstein": "Albert Einstein",
    "what python": "Python programming language",
    "python kya hai": "Python programming language",
    "robotics kya hai": "robotics",
    "ai kya hai": "artificial intelligence"
  },
  "bonus_keywords": {
    "artificial intelligence": 20,
    "robotics": 20,
    "python": 20,
    "solar panel": 20
  },
  "filter_words_regex": "\\b(please|tell me|can you|could you|batao|kya hai)\\b"
}
""".trimIndent()

    fun getAdminConfig(): AdminConfig {
        return AdminConfig(
            adminPassword = prefs.getString(KEY_ADMIN_PASSWORD, "admin123") ?: "admin123",
            wikipediaSearchUrl = prefs.getString(KEY_WIKI_SEARCH_URL, "https://en.wikipedia.org/w/api.php") ?: "https://en.wikipedia.org/w/api.php",
            wikipediaSummaryUrl = prefs.getString(KEY_WIKI_SUMMARY_URL, "https://en.wikipedia.org/api/rest_v1/page/summary/") ?: "https://en.wikipedia.org/api/rest_v1/page/summary/",
            searchLimit = prefs.getInt(KEY_SEARCH_LIMIT, 8),
            customAiApiKey = prefs.getString(KEY_CUSTOM_AI_KEY, "") ?: "",
            customAiEndpoint = prefs.getString(KEY_CUSTOM_AI_ENDPOINT, "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent") ?: "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent",
            customAiModel = prefs.getString(KEY_CUSTOM_AI_MODEL, "gemini-1.5-flash") ?: "gemini-1.5-flash",
            enableHybridAi = prefs.getBoolean(KEY_ENABLE_HYBRID_AI, false),
            systemPrompt = prefs.getString(KEY_SYSTEM_PROMPT, "You are Hamza AI, a smart assistant created by Hamza.") ?: "You are Hamza AI, a smart assistant created by Hamza."
        )
    }

    fun saveAdminConfig(config: AdminConfig) {
        prefs.edit()
            .putString(KEY_ADMIN_PASSWORD, config.adminPassword)
            .putString(KEY_WIKI_SEARCH_URL, config.wikipediaSearchUrl)
            .putString(KEY_WIKI_SUMMARY_URL, config.wikipediaSummaryUrl)
            .putInt(KEY_SEARCH_LIMIT, config.searchLimit)
            .putString(KEY_CUSTOM_AI_KEY, config.customAiApiKey)
            .putString(KEY_CUSTOM_AI_ENDPOINT, config.customAiEndpoint)
            .putString(KEY_CUSTOM_AI_MODEL, config.customAiModel)
            .putBoolean(KEY_ENABLE_HYBRID_AI, config.enableHybridAi)
            .putString(KEY_SYSTEM_PROMPT, config.systemPrompt)
            .apply()
    }

    fun updatePassword(newPassword: String): Boolean {
        if (newPassword.isBlank()) return false
        prefs.edit().putString(KEY_ADMIN_PASSWORD, newPassword).apply()
        return true
    }

    fun getRulesCode(): String {
        return prefs.getString(KEY_RULES_CODE, defaultRulesJson) ?: defaultRulesJson
    }

    fun saveRulesCode(codeJson: String): Boolean {
        return try {
            // Validate that it's valid JSON before saving
            JSONObject(codeJson)
            prefs.edit().putString(KEY_RULES_CODE, codeJson).apply()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun parseRulesConfig(): DynamicRulesConfig {
        val jsonStr = getRulesCode()
        return try {
            val root = JSONObject(jsonStr)
            val replacementsMap = mutableMapOf<String, String>()
            if (root.has("replacements")) {
                val repObj = root.getJSONObject("replacements")
                val keys = repObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    replacementsMap[key] = repObj.getString(key)
                }
            }

            val bonusMap = mutableMapOf<String, Int>()
            if (root.has("bonus_keywords")) {
                val bonusObj = root.getJSONObject("bonus_keywords")
                val keys = bonusObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    bonusMap[key] = bonusObj.getInt(key)
                }
            }

            val filterRegex = if (root.has("filter_words_regex")) {
                root.getString("filter_words_regex")
            } else {
                "\\b(please|tell me|can you|could you|batao|kya hai)\\b"
            }

            DynamicRulesConfig(
                replacements = replacementsMap,
                bonusKeywords = bonusMap,
                filterWordsRegex = filterRegex
            )
        } catch (e: Exception) {
            DynamicRulesConfig()
        }
    }

    fun getOrCreateUserId(): String {
        var id = prefs.getString(KEY_CURRENT_USER_ID, null)
        if (id == null) {
            id = "User_${UUID.randomUUID().toString().take(6).uppercase()}"
            prefs.edit().putString(KEY_CURRENT_USER_ID, id).apply()
        }
        return id
    }
}
