package com.example.baristamessenger.data.mapper

import com.example.baristamessenger.data.local.entity.ChatEntity
import com.example.baristamessenger.domain.model.Chat

// Из базы данных в чистую бизнес-модель
fun ChatEntity.toDomain(): Chat {
    return Chat(
        id = id,
        name = name,
        lastMessage = lastMessage,
        timestamp = timestamp
    )
}

// Из бизнес-модели в сущность базы данных для сохранения
fun Chat.toEntity(): ChatEntity {
    return ChatEntity(
        id = id,
        name = name,
        lastMessage = lastMessage,
        timestamp = timestamp
    )
}