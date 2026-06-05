package com.example.baristamessenger.data // Замени на свой пакет

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("barista_app_prefs", Context.MODE_PRIVATE)

    // Сохраняем состояние после успешного входа
    fun saveLoginState(isLoggedIn: Boolean, userName: String = "") {
        prefs.edit()
            .putBoolean("IS_LOGGED_IN", isLoggedIn)
            .putString("USER_NAME", userName)
            .apply() // apply() сохраняет данные асинхронно
    }

    // Проверяем, авторизован ли пользователь
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("IS_LOGGED_IN", false)
    }

    // Получаем имя текущего пользователя (чтобы передавать в твой WorkspaceScreen)
    fun getUserName(): String {
        return prefs.getString("USER_NAME", "Вы") ?: "Вы"
    }

    // Метод для кнопки "Выйти из аккаунта"
    fun logout() {
        prefs.edit().clear().apply()
    }
}