package com.example.baristamessenger.presentation

sealed class Screen(val route: String) {

    // Остальные экраны приложения
    object Login : Screen("login")
    object Register : Screen("register")
    object Chat : Screen("chat/{chatId}") {
        fun createRoute(chatId: String) = "chat/$chatId"
    }

    object Calculator : Screen("calculator")
    object Exchange : Screen("exchange")
}