package com.example.baristamessenger.domain.repository

import com.example.baristamessenger.domain.model.Message
import kotlinx.coroutines.flow.Flow
import com.example.baristamessenger.domain.model.Chat
import com.example.baristamessenger.domain.model.User

interface MessageRepository {
    // Старые функции для сообщений
    fun getMessages(chatId: String): Flow<List<Message>>
    suspend fun saveMessage(message: Message)

    // ДОБАВЛЯЕМ НАШИ НОВЫЕ ФУНКЦИИ ДЛЯ ЧАТОВ:
    fun getChats(): Flow<List<Chat>> // Получить список всех чатов кофейни
    suspend fun saveChat(chat: Chat)  // Создать или обновить чат
    suspend fun createChat(name: String): Result<Unit>

    suspend fun saveUserProfile(user: User): Result<Unit>
    suspend fun getUserProfile(uid: String): Result<User?>
}