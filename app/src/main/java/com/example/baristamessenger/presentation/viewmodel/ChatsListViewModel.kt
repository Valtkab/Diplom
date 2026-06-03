package com.example.baristamessenger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.baristamessenger.domain.model.Chat
import com.example.baristamessenger.domain.repository.MessageRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatsListViewModel(
    private val repository: MessageRepository
) : ViewModel() {

    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats.asStateFlow()

    init {
        // Оставили только загрузку реальных данных
        loadChats()
    }

    private fun loadChats() {
        viewModelScope.launch {
            repository.getChats().collect { listOfChats ->
                _chats.value = listOfChats
            }
        }
    }

    fun onCreateChatClick(chatName: String) {
        if (chatName.isNotBlank()) {
            viewModelScope.launch {
                repository.createChat(chatName)
            }
        }
    }

    // Удаление чата из Firebase Firestore
    fun deleteChat(chatId: String) {
        FirebaseFirestore.getInstance().collection("chats").document(chatId).delete()
            .addOnFailureListener { /* обработка ошибки */ }
    }

    // Создание нового чата/канала в Firebase Firestore
    fun createChat(name: String, isChannel: Boolean) {
        val newChat = hashMapOf(
            "name" to name,
            "isChannel" to isChannel,
            "lastMessage" to ""
        )
        FirebaseFirestore.getInstance().collection("chats").add(newChat)
    }
}