package com.example.baristamessenger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.baristamessenger.domain.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SearchUserViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _users =
        MutableStateFlow<List<User>>(emptyList())

    val users: StateFlow<List<User>>
            = _users.asStateFlow()

    init {
        loadUsers()
    }

    private fun loadUsers() {

        db.collection("users")
            .get()
            .addOnSuccessListener { result ->

                _users.value =
                    result.documents.mapNotNull {
                        it.toObject(User::class.java)
                    }
            }
    }
}