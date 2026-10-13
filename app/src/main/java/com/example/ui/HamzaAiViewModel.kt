package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AdminConfig
import com.example.data.model.ChatMessage
import com.example.data.model.DynamicRulesConfig
import com.example.data.model.OnlineUser
import com.example.data.model.SenderType
import com.example.data.repository.AdminConfigRepository
import com.example.data.repository.UserSessionRepository
import com.example.data.repository.WikipediaRepository
import com.example.engine.HamzaAiResearchEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HamzaAiViewModel(application: Application) : AndroidViewModel(application) {

    private val adminConfigRepository = AdminConfigRepository(application)
    private val userSessionRepository = UserSessionRepository(application, adminConfigRepository)
    private val wikipediaRepository = WikipediaRepository()
    private val researchEngine = HamzaAiResearchEngine(wikipediaRepository)

    val currentUserId: String = userSessionRepository.getCurrentUserId()

    private val _adminConfig = MutableStateFlow(adminConfigRepository.getAdminConfig())
    val adminConfig: StateFlow<AdminConfig> = _adminConfig.asStateFlow()

    private val _dynamicRules = MutableStateFlow(adminConfigRepository.parseRulesConfig())
    val dynamicRules: StateFlow<DynamicRulesConfig> = _dynamicRules.asStateFlow()

    private val _rulesCodeText = MutableStateFlow(adminConfigRepository.getRulesCode())
    val rulesCodeText: StateFlow<String> = _rulesCodeText.asStateFlow()

    val onlineUsers: StateFlow<List<OnlineUser>> = userSessionRepository.onlineUsers

    private val _statusText = MutableStateFlow("Hamza AI Ready • Internet Research: ON")
    val statusText: StateFlow<String> = _statusText.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isAdminLoggedIn = MutableStateFlow(false)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    private val _showLoginDialog = MutableStateFlow(false)
    val showLoginDialog: StateFlow<Boolean> = _showLoginDialog.asStateFlow()

    private val _showDashboardDialog = MutableStateFlow(false)
    val showDashboardDialog: StateFlow<Boolean> = _showDashboardDialog.asStateFlow()

    init {
        // Initial welcome message matching the user's Python app
        val welcomeMsg = ChatMessage(
            sender = SenderType.ASSISTANT,
            content = "Hello! Welcome to Hamza AI ($currentUserId).\n\nAsk me a question.\n\nExample:\n• What is AI?\n• Robotics kya hai?\n• Who was Einstein?\n• What is Python?\n• How solar panels work"
        )
        _messages.value = listOf(welcomeMsg)
    }

    fun onInputChanged(text: String) {
        _inputText.value = text
    }

    fun askQuestion(query: String? = null) {
        val question = (query ?: _inputText.value).trim()
        if (question.isBlank() || _isSearching.value) return

        _inputText.value = ""
        _isSearching.value = true
        _statusText.value = "Hamza AI searching..."

        userSessionRepository.recordQuery(question)

        val userMsg = ChatMessage(
            sender = SenderType.USER,
            content = question
        )

        val loadingAssistantMsg = ChatMessage(
            sender = SenderType.ASSISTANT,
            content = "Please wait...\nHamza AI is understanding your question and researching online...",
            isLoading = true
        )

        _messages.value = _messages.value + userMsg + loadingAssistantMsg

        viewModelScope.launch {
            try {
                val result = researchEngine.research(
                    rawQuestion = question,
                    config = _adminConfig.value,
                    rules = _dynamicRules.value
                )

                val answerMsg = ChatMessage(
                    id = loadingAssistantMsg.id,
                    sender = SenderType.ASSISTANT,
                    content = result.answer,
                    improvedQuestion = result.improvedQuestion,
                    sources = result.sources,
                    relatedTopics = result.relatedTopics,
                    isLoading = false
                )

                _messages.value = _messages.value.map { msg ->
                    if (msg.id == loadingAssistantMsg.id) answerMsg else msg
                }
            } catch (e: Exception) {
                val errorMsg = ChatMessage(
                    id = loadingAssistantMsg.id,
                    sender = SenderType.ASSISTANT,
                    content = "ERROR\n\n${e.localizedMessage ?: "Failed to retrieve information."}",
                    isLoading = false
                )
                _messages.value = _messages.value.map { msg ->
                    if (msg.id == loadingAssistantMsg.id) errorMsg else msg
                }
            } finally {
                _isSearching.value = false
                _statusText.value = "Hamza AI Ready • Internet Research: ON"
            }
        }
    }

    fun clearChat() {
        val welcomeMsg = ChatMessage(
            sender = SenderType.ASSISTANT,
            content = "Chat cleared. Welcome to Hamza AI ($currentUserId).\nAsk me any question!"
        )
        _messages.value = listOf(welcomeMsg)
    }

    // Admin Flow
    fun onAdminClick() {
        if (_isAdminLoggedIn.value) {
            _showDashboardDialog.value = true
        } else {
            _showLoginDialog.value = true
        }
    }

    fun dismissLoginDialog() {
        _showLoginDialog.value = false
    }

    fun verifyAdminPassword(password: String): Boolean {
        return if (password == _adminConfig.value.adminPassword) {
            _isAdminLoggedIn.value = true
            _showLoginDialog.value = false
            _showDashboardDialog.value = true
            true
        } else {
            false
        }
    }

    fun logoutAdmin() {
        _isAdminLoggedIn.value = false
        _showDashboardDialog.value = false
    }

    fun dismissDashboardDialog() {
        _showDashboardDialog.value = false
    }

    fun changeAdminPassword(newPass: String): Boolean {
        val success = adminConfigRepository.updatePassword(newPass)
        if (success) {
            _adminConfig.value = adminConfigRepository.getAdminConfig()
        }
        return success
    }

    fun saveAdminConfig(updated: AdminConfig) {
        adminConfigRepository.saveAdminConfig(updated)
        _adminConfig.value = updated
    }

    fun saveRulesCode(codeJson: String): Boolean {
        val success = adminConfigRepository.saveRulesCode(codeJson)
        if (success) {
            _rulesCodeText.value = codeJson
            _dynamicRules.value = adminConfigRepository.parseRulesConfig()
        }
        return success
    }

    fun testRuleImprovement(input: String): String {
        return researchEngine.improveQuestion(input, _dynamicRules.value)
    }

    fun addSimulatedUser(): OnlineUser {
        return userSessionRepository.addSimulatedUser()
    }

    fun toggleUserStatus(userId: String) {
        userSessionRepository.toggleUserStatus(userId)
    }
}
