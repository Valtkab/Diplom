package com.example.baristamessenger.domain.model

data class Message(
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val senderName: String = "", // ДОБАВИЛИ ПОЛЕ
    val text: String = "",
    val timestamp: Long = 0L,
    val isRecipe: Boolean = false
)