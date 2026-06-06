package com.example.baristamessenger.domain.model

import com.google.firebase.firestore.DocumentId

data class Message(
    @DocumentId
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val senderName: String = "", // ДОБАВИЛИ ПОЛЕ
    val text: String = "",
    val timestamp: Long = 0L,
    val isRecipe: Boolean = false,
    val senderNickname: String = "",
    val isEdited: Boolean = false,  // ← ДОБАВИТЬ: был ли отредактирован
    val editTimestamp: Long = 0L,
    val reactions: Map<String, Int> = emptyMap()
)