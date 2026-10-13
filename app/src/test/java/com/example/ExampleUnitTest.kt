package com.example

import com.example.data.model.DynamicRulesConfig
import com.example.data.repository.WikipediaRepository
import com.example.engine.HamzaAiResearchEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    private val engine = HamzaAiResearchEngine(WikipediaRepository())
    private val defaultRules = DynamicRulesConfig()

    @Test
    fun testImproveQuestion_directReplacements() {
        assertEquals("artificial intelligence", engine.improveQuestion("what is ai", defaultRules))
        assertEquals("artificial intelligence", engine.improveQuestion("ai kya hai", defaultRules))
        assertEquals("robotics", engine.improveQuestion("robotics kya hai", defaultRules))
        assertEquals("Python programming language", engine.improveQuestion("python kya hai", defaultRules))
        assertEquals("Albert Einstein", engine.improveQuestion("who was einstein", defaultRules))
    }

    @Test
    fun testImproveQuestion_fillerStripping() {
        val raw = "please tell me about quantum computing"
        val improved = engine.improveQuestion(raw, defaultRules)
        assertEquals("about quantum computing", improved)
    }

    @Test
    fun testScoreResult_bonusKeywords() {
        val scoreAi = engine.scoreResult(
            title = "Artificial Intelligence overview",
            summary = "A field of computer science focusing on artificial intelligence systems.",
            question = "artificial intelligence",
            bonusKeywords = defaultRules.bonusKeywords
        )
        // Bonus 20 + word matches in title and summary
        assertTrue("AI score should be at least 20", scoreAi >= 20)
    }

    @Test
    fun testCustomDynamicRules() {
        val customRules = DynamicRulesConfig(
            replacements = mapOf("flutter kya hai" to "Flutter framework"),
            bonusKeywords = mapOf("flutter" to 25)
        )
        val improved = engine.improveQuestion("flutter kya hai", customRules)
        assertEquals("Flutter framework", improved)
    }
}
