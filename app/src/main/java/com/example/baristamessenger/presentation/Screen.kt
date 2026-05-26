package com.example.baristamessenger.presentation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object ChatsList : Screen("chats_list")
    object Chat : Screen("chat/{chatId}") {
        fun createRoute(chatId: String) = "chat/$chatId"
    }
    object Register : Screen("register")
    object Profile : Screen("profile_screen")
}