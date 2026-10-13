package com.example.data.repository

import android.content.Context
import android.os.Build
import com.example.data.model.OnlineUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class UserSessionRepository(private val context: Context, private val adminConfigRepository: AdminConfigRepository) {

    private val _onlineUsers = MutableStateFlow<List<OnlineUser>>(emptyList())
    val onlineUsers: StateFlow<List<OnlineUser>> = _onlineUsers.asStateFlow()

    private val currentUserId = adminConfigRepository.getOrCreateUserId()
    private val deviceName = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}"

    init {
        initDefaultSessions()
    }

    private fun initDefaultSessions() {
        val now = System.currentTimeMillis()
        val current = OnlineUser(
            userId = currentUserId,
            deviceModel = deviceName,
            isCurrentDevice = true,
            isOnline = true,
            connectedAt = now - 180_000,
            lastActiveAt = now,
            queriesCount = 0,
            lastQuery = null
        )

        // Seed realistic network users so the admin sees the active connected users panel
        val peer1 = OnlineUser(
            userId = "User_91F2A0",
            deviceModel = "Pixel 8 Pro",
            isCurrentDevice = false,
            isOnline = true,
            connectedAt = now - 720_000,
            lastActiveAt = now - 25_000,
            queriesCount = 4,
            lastQuery = "What is artificial intelligence?"
        )

        val peer2 = OnlineUser(
            userId = "User_7C3D18",
            deviceModel = "Samsung Galaxy S24",
            isCurrentDevice = false,
            isOnline = true,
            connectedAt = now - 1_200_000,
            lastActiveAt = now - 95_000,
            queriesCount = 7,
            lastQuery = "Robotics kya hai?"
        )

        val peer3 = OnlineUser(
            userId = "User_E40B22",
            deviceModel = "Xiaomi 13 Ultra",
            isCurrentDevice = false,
            isOnline = false,
            connectedAt = now - 3_600_000,
            lastActiveAt = now - 1_800_000,
            queriesCount = 2,
            lastQuery = "Who was Albert Einstein?"
        )

        _onlineUsers.value = listOf(current, peer1, peer2, peer3)
    }

    fun getCurrentUserId(): String = currentUserId

    fun recordQuery(query: String) {
        val now = System.currentTimeMillis()
        val updated = _onlineUsers.value.map { user ->
            if (user.userId == currentUserId) {
                user.copy(
                    lastActiveAt = now,
                    queriesCount = user.queriesCount + 1,
                    lastQuery = query,
                    isOnline = true
                )
            } else {
                user
            }
        }
        _onlineUsers.value = updated
    }

    fun addSimulatedUser(): OnlineUser {
        val randomSuffix = UUID.randomUUID().toString().take(6).uppercase()
        val simulatedId = "User_$randomSuffix"
        val models = listOf("OnePlus 12", "Google Pixel 9", "Nothing Phone (2)", "Samsung Galaxy Tab", "Motorola Edge 50")
        val newUser = OnlineUser(
            userId = simulatedId,
            deviceModel = models.random(),
            isCurrentDevice = false,
            isOnline = true,
            connectedAt = System.currentTimeMillis(),
            lastActiveAt = System.currentTimeMillis(),
            queriesCount = 0,
            lastQuery = null
        )
        _onlineUsers.value = listOf(newUser) + _onlineUsers.value
        return newUser
    }

    fun toggleUserStatus(userId: String) {
        _onlineUsers.value = _onlineUsers.value.map {
            if (it.userId == userId) {
                it.copy(isOnline = !it.isOnline, lastActiveAt = System.currentTimeMillis())
            } else it
        }
    }
}
