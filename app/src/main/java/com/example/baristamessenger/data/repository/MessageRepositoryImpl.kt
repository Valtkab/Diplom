package com.example.baristamessenger.data.repository

import com.example.baristamessenger.data.local.MessageDao
import com.example.baristamessenger.data.mapper.toEntity
import com.example.baristamessenger.domain.model.Chat
import com.example.baristamessenger.domain.model.Message
import com.example.baristamessenger.domain.model.User
import com.example.baristamessenger.domain.repository.MessageRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class MessageRepositoryImpl(
    private val messageDao: MessageDao
) : MessageRepository {

    private val firestore = FirebaseFirestore.getInstance()

    // ==========================================
    // НАСТРОЙКА РАБОТЫ С СООБЩЕНИЯМИ (MESSAGES)
    // ==========================================

    override fun getMessages(chatId: String): Flow<List<Message>> = callbackFlow {
        val listener = firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val messagesList = snapshot.documents.map { doc ->
                        Message(
                            id = doc.id,
                            chatId = chatId,
                            senderId = doc.getString("senderId") ?: "",
                            senderName = doc.getString("senderName") ?: "Аноним",
                            text = doc.getString("text") ?: "",
                            timestamp = try {
                                doc.getLong("timestamp") ?: doc.getString("timestamp")?.toLongOrNull() ?: 0L
                            } catch (e: Exception) {
                                0L
                            },
                            isRecipe = doc.getBoolean("isRecipe") ?: false
                        )
                    }
                    trySend(messagesList)
                }
            }

        awaitClose { listener.remove() }
    }

    override suspend fun saveMessage(message: Message) {
        messageDao.insertMessage(message.toEntity())

        val messageMap = hashMapOf(
            "senderId" to message.senderId,
            "senderName" to message.senderName, // ДОБАВИЛИ СЮДА
            "text" to message.text,
            "timestamp" to message.timestamp,
            "isRecipe" to message.isRecipe
        )

        try {
            firestore.collection("chats")
                .document(message.chatId)
                .collection("messages")
                .add(messageMap)
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ==========================================
    // НАСТРОЙКА РАБОТЫ С ЧАТАМИ (CHATS)
    // ==========================================

    override fun getChats(): Flow<List<Chat>> = callbackFlow {
        val listener = firestore.collection("chats")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val chatsList = snapshot.documents.map { doc ->
                        Chat(
                            id = doc.id,
                            name = doc.getString("name") ?: "Рабочий чат",
                            lastMessage = doc.getString("lastMessage") ?: "",
                            timestamp = try {
                                doc.getLong("timestamp") ?: doc.getString("timestamp")?.toLongOrNull() ?: 0L
                            } catch (e: Exception) {
                                0L
                            }
                        )
                    }
                    trySend(chatsList)
                }
            }

        awaitClose { listener.remove() }
    }

    override suspend fun saveChat(chat: Chat) {
        val chatMap = hashMapOf(
            "name" to chat.name,
            "lastMessage" to chat.lastMessage,
            "timestamp" to chat.timestamp
        )

        try {
            firestore.collection("chats")
                .document(chat.id)
                .set(chatMap)
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun createChat(name: String): Result<Unit> = try {
        val chatCollection = firestore.collection("chats")
        val newChatId = chatCollection.document().id

        val newChat = mapOf(
            "id" to newChatId,
            "name" to name,
            "lastMessage" to "Чат создан",
            "timestamp" to System.currentTimeMillis()
        )

        chatCollection.document(newChatId).set(newChat).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ==========================================
    // НАСТРОЙКА ПРОФИЛЯ ПОЛЬЗОВАТЕЛЯ (USER)
    // ==========================================

    override suspend fun saveUserProfile(user: User): Result<Unit> = try {
        firestore.collection("users").document(user.id).set(user).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getUserProfile(uid: String): Result<User?> = try {
        val snapshot = firestore.collection("users").document(uid).get().await()
        val user = snapshot.toObject(User::class.java)
        Result.success(user)
    } catch (e: Exception) {
        Result.failure(e)
    }
}