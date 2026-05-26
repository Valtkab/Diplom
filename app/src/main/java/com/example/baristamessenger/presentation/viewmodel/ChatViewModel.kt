package com.example.baristamessenger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.baristamessenger.domain.model.Message
import com.example.baristamessenger.domain.repository.MessageRepository
import com.google.firebase.auth.FirebaseAuth
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
        viewModelScope.launch {
            repository.getMessages(chatId).collect { listOfMessages ->
                _messages.value = listOfMessages
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
}