package com.example.baristamessenger.presentation

sealed class Screen(val route: String) {
    // 4 Главных экрана нижней панели
    object ChatsList : Screen("chats_list")
    object ToolsHub : Screen("tools_hub")      // Вместо старой Биржи Смен
    object Notifications : Screen("notifications") // Вместо Инструкции
    object Profile : Screen("profile_screen")

    // Остальные экраны приложения
    object Login : Screen("login")
    object Register : Screen("register")
    object Chat : Screen("chat/{chatId}") {
        fun createRoute(chatId: String) = "chat/$chatId"
    }

    // Под-экраны, которые открываются ИЗ Инструментов
    object Market : Screen("market")
    object Help : Screen("help")
    object Calculator : Screen("calculator")
    object Exchange : Screen("exchange")
}