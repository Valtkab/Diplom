package com.example.baristamessenger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.baristamessenger.domain.model.Chat
import com.example.baristamessenger.domain.repository.MessageRepository
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
        loadChats()
        generateTestChats() // Временно создаем фейковые чаты для проверки интерфейса
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

    // Метод создания тестовых данных
    private fun generateTestChats() {
        viewModelScope.launch {
            val testChats = listOf(
                Chat("chat_1", "Общий чат смены ☕", "Кто оставил грязный питчер?", System.currentTimeMillis()),
                Chat("chat_2", "Шеф-бариста / Рецепты", "Обновили ТТК на айс-латте", System.currentTimeMillis() - 100000),
                Chat("chat_3", "Заказ зерна и молока 🥛", "Молоко приедет к 10:00", System.currentTimeMillis() - 500000)
            )
            // Сохраняем каждый тестовый чат в Room базу данных
            testChats.forEach { repository.saveChat(it) }
        }
    }
}