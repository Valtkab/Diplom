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
import kotlinx.coroutines.tasks.await

class AuthViewModel(
    private val repository: MessageRepository
) : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _loginState = MutableStateFlow<StateResult?>(null)
    val loginState: StateFlow<StateResult?> = _loginState.asStateFlow()

    // ВХОД В СИСТЕМУ
    fun login(email: String, javaScriptPasswordText: String) {
        if (email.isBlank() || javaScriptPasswordText.isBlank()) {
            _loginState.value = StateResult.Error("Заполните все поля")
            return
        }

        _loginState.value = StateResult.Loading

        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, javaScriptPasswordText).await()
                _loginState.value = StateResult.Success
            } catch (e: Exception) {
                _loginState.value = StateResult.Error(e.localizedMessage ?: "Ошибка авторизации")
            }
        }
    }

    // РЕГИСТРАЦИЯ НОВОГО БАРИСТА
    fun register(
        firstName: String,
        lastName: String,
        nickname: String,
        email: String,
        javaScriptPasswordText: String
    ) {
        // Проверяем, что вообще ВСЕ поля заполнены
        if (firstName.isBlank() || lastName.isBlank() || nickname.isBlank() || email.isBlank() || javaScriptPasswordText.isBlank()) {
            _loginState.value = StateResult.Error("Заполните абсолютно все поля")
            return
        }
        if (javaScriptPasswordText.length < 6) {
            _loginState.value = StateResult.Error("Пароль должен быть не менее 6 символов")
            return
        }

        _loginState.value = StateResult.Loading

        viewModelScope.launch {
            try {
                val authResult = auth.createUserWithEmailAndPassword(email, javaScriptPasswordText).await()
                val uid = authResult.user?.uid ?: ""

                // Создаем полноценный профиль на основе твоей модели User
                val newUser = User(
                    id = uid,
                    firstName = firstName,  // или раздели имя и фамилию
                    lastName = lastName,
                    nickname = nickname,
                    imageUrl = "", // Заглушка для аватара
                    role = "Бариста", // Базовая роль для новичка
                    coffeeShop = nickname // Временно сохраняем никнейм в это поле (или в будущем расширишь модель)
                )

                repository.saveUserProfile(newUser)
                _loginState.value = StateResult.Success
            } catch (e: Exception) {
                _loginState.value = StateResult.Error(e.localizedMessage ?: "Ошибка регистрации")
            }
        }
    }

    fun resetState() {
        _loginState.value = null
    }

    // ВЕРНУЛИ НА МЕСТО: Вспомогательные классы теперь строго внутри ViewModel
    sealed interface StateResult {
        object Loading : StateResult
        object Success : StateResult
        data class Error(val errorMessage: String) : StateResult // Переименовали в errorMessage для надежности
    }
}