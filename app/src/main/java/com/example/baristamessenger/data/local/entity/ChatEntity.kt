package com.example.baristamessenger.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val id: String, // ID чата будет первичным ключом (ключ-уникатор)
    val name: String,
    val lastMessage: String,
    val timestamp: Long
)