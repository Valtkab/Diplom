package com.example.baristamessenger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.baristamessenger.domain.model.Chat
import com.example.baristamessenger.domain.repository.MessageRepository
import com.google.firebase.auth.FirebaseAuth
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
        loadChats()
    }

    private fun loadChats() {
        viewModelScope.launch {
            repository.getChats().collect { listOfChats ->
                _chats.value = listOfChats
            }
        }
    }



    fun createPrivateChat(
        selectedUserId: String,
        selectedUserName: String
    ) {

        val currentUserId =
            com.google.firebase.auth.FirebaseAuth
                .getInstance()
                .currentUser
                ?.uid ?: return

        val participants = listOf(currentUserId, selectedUserId)

        val chatsRef = FirebaseFirestore.getInstance()
            .collection("chats")

        // 🔥 1. Ищем уже существующий чат
        chatsRef
            .whereArrayContains("participants", currentUserId)
            .get()
            .addOnSuccessListener { snapshot ->

                val existingChat = snapshot.documents.firstOrNull { doc ->

                    val list = doc.get("participants") as? List<String>
                        ?: emptyList()

                    list.containsAll(participants) && list.size == 2
                }

                if (existingChat != null) {
                    // 🔥 ЧАТ УЖЕ ЕСТЬ → НИЧЕГО НЕ СОЗДАЁМ
                    return@addOnSuccessListener
                }

                // 🔥 2. Если чата нет → создаём новый
                val chat = hashMapOf(
                    "name" to selectedUserName,
                    "lastMessage" to "",
                    "timestamp" to System.currentTimeMillis(),
                    "participants" to participants,
                    "isGroup" to false,
                    "isChannel" to false
                )

                chatsRef.add(chat)
            }
    }

    fun createGroupChat(
        name: String,
        selectedUsers: List<String>
    ) {

        val currentUserId =
            FirebaseAuth.getInstance().currentUser?.uid ?: return

        val participants = (selectedUsers + currentUserId).distinct()

        val chat = hashMapOf(
            "name" to name,
            "lastMessage" to "",
            "timestamp" to System.currentTimeMillis(),
            "participants" to participants,
            "isGroup" to true,
            "isChannel" to false,
            "type" to "group"
        )

        FirebaseFirestore.getInstance()
            .collection("chats")
            .add(chat)
    }
}