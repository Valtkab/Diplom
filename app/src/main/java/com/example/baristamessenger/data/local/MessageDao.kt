package com.example.baristamessenger.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.baristamessenger.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow
import com.example.baristamessenger.data.local.entity.ChatEntity

@Dao
interface MessageDao {

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun getMessages(chatId: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("DELETE FROM messages WHERE chatId = :chatId")
    suspend fun clearChatHistory(chatId: String)

    @Query("SELECT * FROM chats ORDER BY timestamp DESC")
    fun getChats(): kotlinx.coroutines.flow.Flow<List<ChatEntity>>
// ORDER BY timestamp DESC означает: "отсортируй чаты по времени от самых новых к старым"

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatEntity)
// OnConflictStrategy.REPLACE означает: "если чат с таким ID уже есть, просто обнови его данные"
}