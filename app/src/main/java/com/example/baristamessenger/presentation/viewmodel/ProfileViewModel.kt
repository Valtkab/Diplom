package com.example.baristamessenger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.baristamessenger.domain.model.User
import com.example.baristamessenger.domain.repository.MessageRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: MessageRepository
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            _userProfile.value = User(id = "", name = "Ошибка сессии", imageUrl = "", role = "", coffeeShop = "Не вошел")
            return
        }

        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    try {
                        // Достаем имя и фамилию (пробуем разные варианты полей, которые могли записаться при регистрации)
                        val fName = document.getString("firstName") ?: document.getString("name") ?: "Бариста"
                        val lName = document.getString("lastName") ?: ""
                        val fullName = if (lName.isNotEmpty()) "$fName $lName" else fName

                        // Достаем никнейм (проверяем поля nickname или coffeeShop)
                        val nick = document.getString("nickname") ?: document.getString("coffeeShop") ?: "@barista"
                        val roleName = document.getString("role") ?: "Бариста"

                        // Собираем объект User вручную БЕЗ toObject(), чтобы приложение НЕ ВЫЛЕТАЛО
                        _userProfile.value = User(
                            id = uid,
                            name = fullName,
                            imageUrl = "",
                            role = roleName,
                            coffeeShop = nick
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // Если внутри блока try что-то пошло не так, приложение не упадет, а покажет заглушку
                        _userProfile.value = User(
                            id = uid,
                            name = "Ошибка структуры данных",
                            imageUrl = "",
                            role = "Бариста",
                            coffeeShop = "@error"
                        )
                    }
                } else {
                    // Если документа в Firestore вообще нет
                    _userProfile.value = User(
                        id = uid,
                        name = auth.currentUser?.email ?: "Новый Бариста",
                        imageUrl = "",
                        role = "Бариста",
                        coffeeShop = "@nickname"
                    )
                }
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
                _userProfile.value = User(
                    id = uid,
                    name = "Ошибка сети",
                    imageUrl = "",
                    role = "Проверьте интернет",
                    coffeeShop = ""
                )
            }
    }

    fun logout(onLogoutSuccess: () -> Unit) {
        auth.signOut()
        onLogoutSuccess()
    }
}