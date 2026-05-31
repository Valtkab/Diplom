package com.example.baristamessenger.domain.model

data class Chat(
    val id: String,          // Уникальный ID чата (например, "chat_general")
    val name: String,        // Название чата (например, "Общий чат смены ☕")
    val lastMessage: String, // Текст последнего сообщения для превью на экране списка
    val timestamp: Long,     // Время последнего сообщения (чтобы сортировать чаты от свежих к старым)\
    val isChannel: Boolean = false
)