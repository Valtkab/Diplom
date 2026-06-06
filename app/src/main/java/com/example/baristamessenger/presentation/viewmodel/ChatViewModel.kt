package com.example.baristamessenger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.baristamessenger.domain.model.Message
import com.example.baristamessenger.domain.model.User
import com.example.baristamessenger.domain.repository.MessageRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import com.google.firebase.storage.FirebaseStorage

class ChatViewModel(
    private val repository: MessageRepository
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _currentUserNickname = MutableStateFlow("")
    val currentUserNickname: StateFlow<String> = _currentUserNickname.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()



    init {
        loadCurrentUserNickname()
    }

    private fun loadCurrentUserNickname() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Сначала пробуем получить nickname, потом coffeeShop, потом email
                    val nickname = document.getString("nickname")
                        ?: document.getString("coffeeShop")
                        ?: document.getString("email")?.substringBefore("@")
                        ?: "Бариста"
                    _currentUserNickname.value = nickname
                } else {
                    _currentUserNickname.value = "Бариста"
                }
            }
            .addOnFailureListener {
                _currentUserNickname.value = "Бариста"
            }
    }

    fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: "unknown_barista"
    }

    fun loadMessages(chatId: String) {
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp")  // ← добавляем сортировку по времени
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _messages.value = snapshot.documents
                        .mapNotNull { doc ->
                            doc.toObject(Message::class.java)?.copy(id = doc.id)
                        }
                }
            }
    }

    fun sendMessage(chatId: String, text: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val currentNick = _currentUserNickname.value  // ← убедись, что эта переменная есть

        if (currentNick.isEmpty()) {
            // Если никнейм ещё не загружен, загружаем и отправляем
            loadCurrentUserNicknameAndSend(chatId, text)
            return
        }

        val tempId = UUID.randomUUID().toString()

        val newMessage = Message(
            id = tempId,
            chatId = chatId,
            senderId = uid,
            senderNickname = currentNick,  // ← КЛЮЧЕВОЕ: сохраняем никнейм
            text = text,
            timestamp = System.currentTimeMillis(),
            isRecipe = false
        )

        _messages.value = _messages.value + newMessage

        viewModelScope.launch {
            try {
                repository.saveMessage(newMessage)
            } catch (e: Exception) {
                _messages.value = _messages.value.filterNot { it.id == tempId }
            }
        }
    }

    private fun loadCurrentUserNicknameAndSend(chatId: String, text: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                val nickname = document.getString("nickname")
                    ?: document.getString("coffeeShop")
                    ?: document.getString("email")?.substringBefore("@")
                    ?: "Бариста"

                _currentUserNickname.value = nickname

                // Отправляем сообщение с никнеймом
                sendMessage(chatId, text)
            }
            .addOnFailureListener {
                sendMessage(chatId, text) // Повторная попытка с дефолтным значением
            }
    }

    fun deleteMessage(chatId: String, messageId: String) {
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .document(messageId)
            .delete()
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }

    fun deleteMessageWithImage(chatId: String, message: Message) {
        // 1. Если в сообщении есть фото, извлекаем путь и удаляем файл
        if (message.text.contains("📸описание:")) {
            val imageUrl = message.text.substringAfter("📸описание:").trim()

            try {
                val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
                storageRef.delete().addOnSuccessListener {
                    deleteMessageFromFirestore(chatId, message.id)
                }.addOnFailureListener {
                    deleteMessageFromFirestore(chatId, message.id)
                }
            } catch (e: Exception) {
                deleteMessageFromFirestore(chatId, message.id)
            }
        } else {
            deleteMessageFromFirestore(chatId, message.id)
        }
    }

    private fun deleteMessageFromFirestore(chatId: String, messageId: String) {
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .document(messageId)
            .delete()
    }

    fun migrateOldMessages() {
        val db = FirebaseFirestore.getInstance()

        db.collectionGroup("messages").get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    // Проверяем, есть ли поле senderNickname
                    if (!doc.contains("senderNickname") || doc.getString("senderNickname").isNullOrEmpty()) {
                        val senderId = doc.getString("senderId") ?: continue

                        // Получаем никнейм из users
                        db.collection("users").document(senderId).get()
                            .addOnSuccessListener { userDoc ->
                                val nickname = userDoc.getString("nickname")
                                    ?: userDoc.getString("coffeeShop")
                                    ?: userDoc.getString("email")?.substringBefore("@")
                                    ?: "Бариста"

                                doc.reference.update("senderNickname", nickname)
                                    .addOnSuccessListener {
                                        println("Обновлено сообщение ${doc.id}: $nickname")
                                    }
                            }
                    }
                }
            }
            .addOnFailureListener { e ->
                println("Ошибка миграции: ${e.message}")
            }
    }

    fun editMessage(chatId: String, messageId: String, newText: String) {
        val uid = getCurrentUserId()
        if (uid == "unknown_barista") return

        val db = FirebaseFirestore.getInstance()
        val messageRef = db.collection("chats")
            .document(chatId)
            .collection("messages")
            .document(messageId)

        // Проверяем, что пользователь - автор сообщения
        messageRef.get()
            .addOnSuccessListener { document ->
                val senderId = document.getString("senderId")
                if (senderId == uid) {
                    // Обновляем сообщение
                    val updates = mapOf(
                        "text" to newText,
                        "isEdited" to true,
                        "editTimestamp" to System.currentTimeMillis()
                    )
                    messageRef.update(updates)
                        .addOnSuccessListener {
                            // Обновляем локальный список
                            val updatedMessages = _messages.value.map { message ->
                                if (message.id == messageId) {
                                    message.copy(
                                        text = newText,
                                        isEdited = true,
                                        editTimestamp = System.currentTimeMillis()
                                    )
                                } else {
                                    message
                                }
                            }
                            _messages.value = updatedMessages
                        }
                        .addOnFailureListener { e ->
                            println("Ошибка редактирования: ${e.message}")
                        }
                } else {
                    println("Нельзя редактировать чужое сообщение")
                }
            }
    }
}