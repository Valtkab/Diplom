package com.example.baristamessenger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.baristamessenger.domain.model.User
import com.example.baristamessenger.domain.model.UserRole // 1. Импортируем наши роли
import com.example.baristamessenger.domain.repository.MessageRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: MessageRepository
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            // Исправлено: передаем дефолтную роль при ошибке
            _userProfile.value = User(id = "", firstName = "Ошибка", lastName = "", nickname = "", role = UserRole.BARISTA)
            _isLoading.value = false
            return
        }

        _isLoading.value = true
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Безопасно конвертируем строку из базы в наш Enum ролей
                    val roleStr = document.getString("role") ?: "BARISTA"
                    val userRole = when (roleStr.uppercase()) {
                        "MANAGER", "УПРАВЛЯЮЩИЙ" -> UserRole.MANAGER
                        else -> UserRole.BARISTA
                    }

                    val user = User(
                        id = uid,
                        firstName = document.getString("firstName") ?: "",
                        lastName = document.getString("lastName") ?: "",
                        nickname = document.getString("nickname") ?: "",
                        birthDate = document.getString("birthDate") ?: "",
                        aboutMe = document.getString("aboutMe") ?: "",
                        imageUrl = document.getString("imageUrl") ?: "",
                        role = userRole, // Передаем корректный тип UserRole
                        coffeeShop = document.getString("coffeeShop") ?: ""
                    )
                    _userProfile.value = user
                } else {
                    // Новый пользователь — создаём пустую запись с базовой ролью бариста
                    val newUser = User(id = uid, role = UserRole.BARISTA)
                    _userProfile.value = newUser
                    saveNewUserToFirestore(newUser)
                }
                _isLoading.value = false
            }
            .addOnFailureListener { e ->
                _error.value = e.message ?: "Ошибка загрузки"
                _isLoading.value = false
            }
    }

    private fun saveNewUserToFirestore(user: User) {
        val userMap = hashMapOf(
            "firstName" to user.firstName,
            "lastName" to user.lastName,
            "nickname" to user.nickname,
            "birthDate" to user.birthDate,
            "aboutMe" to user.aboutMe,
            "imageUrl" to user.imageUrl,
            "role" to user.role.name, // Сохраняем в Firebase как строку ("BARISTA" или "MANAGER")
            "coffeeShop" to user.coffeeShop
        )
        db.collection("users").document(user.id).set(userMap)
            .addOnFailureListener { e ->
                _error.value = "Не удалось создать профиль: ${e.message}"
            }
    }

    fun saveUserProfile(
        firstName: String,
        lastName: String,
        nickname: String,
        birthDate: String,
        aboutMe: String,
        onSuccess: () -> Unit = {}
    ) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            _error.value = "Пользователь не авторизован"
            return
        }

        _isSaving.value = true
        _error.value = null

        val updates = hashMapOf<String, Any>(
            "firstName" to firstName,
            "lastName" to lastName,
            "nickname" to nickname,
            "birthDate" to birthDate,
            "aboutMe" to aboutMe
        )

        db.collection("users").document(uid)
            .update(updates)
            .addOnSuccessListener {
                val currentUser = _userProfile.value
                _userProfile.value = currentUser?.copy(
                    firstName = firstName,
                    lastName = lastName,
                    nickname = nickname,
                    birthDate = birthDate,
                    aboutMe = aboutMe
                ) ?: User(
                    id = uid,
                    firstName = firstName,
                    lastName = lastName,
                    nickname = nickname,
                    birthDate = birthDate,
                    aboutMe = aboutMe,
                    role = UserRole.BARISTA // Дефолтное значение для фолбэка
                )
                _isSaving.value = false
                onSuccess()
            }
            .addOnFailureListener { e ->
                _error.value = e.message ?: "Ошибка сохранения"
                _isSaving.value = false
            }
    }

    fun logout(onLogoutSuccess: () -> Unit) {
        auth.signOut()
        onLogoutSuccess()
    }

    fun clearError() {
        _error.value = null
    }
}