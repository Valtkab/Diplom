package com.example.baristamessenger.domain.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

class User(
    val id: String = "",
    val firstName: String = "",      // имя
    val lastName: String = "",       // фамилия
    val nickname: String = "",       // никнейм
    val birthDate: String = "",      // дата рождения
    val aboutMe: String = "",        // о себе
    val imageUrl: String = "",
    role: Any? = UserRole.BARISTA,   // 🔥 Вернули имя 'role'! Теперь ViewModels довольны
    val coffeeShop: String = ""      // если нужно для бара
) {
    // Сохраняем логику сейфа: переводим всё в безопасную строку для базы данных
    @get:PropertyName("role")
    @set:PropertyName("role")
    var roleString: String = when (role) {
        is UserRole -> role.name
        is String -> role
        else -> "BARISTA"
    }

    // Логика переводчика: приложение будет видеть строгий красивый Enum
    @get:Exclude
    val role: UserRole
        get() = when (roleString.uppercase()) {
            "MANAGER", "УПРАВЛЯЮЩИЙ" -> UserRole.MANAGER
            else -> UserRole.BARISTA
        }

    @get:Exclude
    val fullName: String
        get() = if (lastName.isNotEmpty()) "$firstName $lastName" else firstName

    // Метод copy для железной совместимости с твоими ViewModel
    fun copy(
        id: String = this.id,
        firstName: String = this.firstName,
        lastName: String = this.lastName,
        nickname: String = this.nickname,
        birthDate: String = this.birthDate,
        aboutMe: String = this.aboutMe,
        imageUrl: String = this.imageUrl,
        role: UserRole = this.role,
        coffeeShop: String = this.coffeeShop
    ): User {
        return User(id, firstName, lastName, nickname, birthDate, aboutMe, imageUrl, role, coffeeShop)
    }
}