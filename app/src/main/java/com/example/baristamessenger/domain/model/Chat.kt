package com.example.baristamessenger.domain.model

data class Chat(
    val id: String = "",
    val name: String = "",
    val lastMessage: String = "",
    val timestamp: Long = 0L,

    val type: String = "chat",
    val isChannel: Boolean = false,

    val participants: List<String> = emptyList(),

    val createdBy: String = "",

    val isGroup: Boolean = false
)


