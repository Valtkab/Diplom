package com.example.baristamessenger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.baristamessenger.domain.model.Message
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

    // Легко получаем экземпляр FirebaseAuth
    private var currentNickname = "@barista"
    private val auth = com.google.firebase.auth.FirebaseAuth.getInstance()

    init {
        // При старте чата подтягиваем никнейм текущего пользователя
        viewModelScope.launch {
            auth.currentUser?.uid?.let { uid ->
                repository.getUserProfile(uid).onSuccess { user ->
                    if (user != null) {
                        // Используем поле coffeeShop, куда мы временно сохранили никнейм при регистрации
                        currentNickname = user.coffeeShop
                    }
                }
            }
        }
    }



    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    // Функция возвращает реальный UID вошедшего бариста (или "unknown", если что-то пошло не так)
    fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: "unknown_barista"
    }

    fun loadMessages(chatId: String) {
        FirebaseFirestore.getInstance()
            .collection("chats")
            .document(chatId)
            .collection("messages")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    // Преобразуем документы в список Message
                    _messages.value =
                        snapshot.documents
                            .mapNotNull { doc ->
                                doc.toObject(Message::class.java)?.copy(
                                    id = doc.id
                                )
                            }
                            .sortedBy { it.timestamp }
                }
            }
    }

    fun sendMessage(chatId: String, text: String) {

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        val tempId = UUID.randomUUID().toString()

        val newMessage = Message(
            id = tempId,
            chatId = chatId,
            senderId = uid,
            text = text,
            timestamp = System.currentTimeMillis(),
            isRecipe = false
        )

        // Мгновенно показываем сообщение в чате
        _messages.value = _messages.value + newMessage

        viewModelScope.launch {
            try {
                repository.saveMessage(newMessage)
            } catch (e: Exception) {
                e.printStackTrace()

                // если отправка не удалась — убираем сообщение
                _messages.value =
                    _messages.value.filterNot { it.id == tempId }
            }
        }
    }

    fun deleteMessage(chatId: String, messageId: String) {
        // Эта функция физически удаляет сообщение из Firebase
        FirebaseFirestore.getInstance()
            .collection("chats")
            .document(chatId)
            .collection("messages")
            .document(messageId)
            .delete()
            .addOnSuccessListener {
                // Удаление прошло успешно
            }
            .addOnFailureListener { e ->
                // Если возникла ошибка, она появится в Logcat
                e.printStackTrace()
            }
    }

    fun deleteMessageWithImage(chatId: String, message: Message) {
        val db = FirebaseFirestore.getInstance()

        // 1. Если в сообщении есть фото, извлекаем путь и удаляем файл
        if (message.text.contains("📸описание:")) {
            val imageUrl = message.text.substringAfter("📸описание:").trim()

            try {
                // Получаем ссылку на файл в Storage по его URL
                val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
                storageRef.delete().addOnSuccessListener {
                    // Файл удален, теперь удаляем сообщение из Firestore
                    deleteMessageFromFirestore(db, chatId, message.id)
                }.addOnFailureListener {
                    // Если файл не найден, все равно удаляем сообщение
                    deleteMessageFromFirestore(db, chatId, message.id)
                }
            } catch (e: Exception) {
                // Если URL некорректный, просто удаляем сообщение
                deleteMessageFromFirestore(db, chatId, message.id)
            }
        } else {
            // Если фото нет, просто удаляем сообщение
            deleteMessageFromFirestore(db, chatId, message.id)
        }
    }

    private fun deleteMessageFromFirestore(db: FirebaseFirestore, chatId: String, messageId: String) {
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .document(messageId)
            .delete()
    }
}