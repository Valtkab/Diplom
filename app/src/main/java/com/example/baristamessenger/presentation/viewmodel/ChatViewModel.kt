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
                    _messages.value = snapshot.documents.mapNotNull { it.toObject(Message::class.java) }
                }
            }
    }

    fun sendMessage(chatId: String, text: String) {
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // Собираем сообщение строго по твоей модели Message
        val newMessage = Message(
            id = "",
            chatId = chatId,
            senderId = uid,
            text = text,
            timestamp = System.currentTimeMillis(),
            isRecipe = false // Возвращаем параметр, который требовал ChatScreen
        )

        viewModelScope.launch {
            // ИСПРАВЛЕНО: вызываем saveMessage вместо sendMessage, как прописано в репозитории
            repository.saveMessage(newMessage)
        }
    }
    fun deleteMessage(chatId: String, messageId: String) {
        if (messageId.isEmpty()) return

        FirebaseFirestore.getInstance()
            .collection("chats")
            .document(chatId)
            .collection("messages")
            .document(messageId)
            .delete()
            .addOnSuccessListener {
                // После успешного удаления в Firebase,
                // Firebase автоматически пришлет обновление в SnapshotListener,
                // и список сообщений обновится сам собой.
            }
            .addOnFailureListener { e ->
                e.printStackTrace() // Посмотри в Logcat, если здесь ошибка!
            }
    }
}