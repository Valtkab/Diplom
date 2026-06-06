package com.example.baristamessenger.domain.model

data class User(
    val id: String = "",
    val firstName: String = "",      // имя
    val lastName: String = "",       // фамилия
    val nickname: String = "",       // никнейм (отдельно)
    val birthDate: String = "",      // дата рождения
    val aboutMe: String = "",        // о себе
    val imageUrl: String = "",
    val role: String = "",
    val coffeeShop: String = ""      // если нужно для бара
) {
    // Вспомогательное поле для отображения полного имени
    val fullName: String
        get() = if (lastName.isNotEmpty()) "$firstName $lastName" else firstName
}