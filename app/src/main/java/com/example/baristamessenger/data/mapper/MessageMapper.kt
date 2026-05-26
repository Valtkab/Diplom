package com.example.baristamessenger.data.mapper

import com.example.baristamessenger.data.local.entity.MessageEntity
import com.example.baristamessenger.domain.model.Message

fun MessageEntity.toDomain(): Message {
    return Message(
        id = id,
        chatId = chatId,
        senderId = senderId,
        text = text,
        timestamp = timestamp,
        isRecipe = isRecipe
    )
}

fun Message.toEntity(): MessageEntity {
    return MessageEntity(
        id = id,
        chatId = chatId,
        senderId = senderId,
        text = text,
        timestamp = timestamp,
        isRecipe = isRecipe
    )
}

