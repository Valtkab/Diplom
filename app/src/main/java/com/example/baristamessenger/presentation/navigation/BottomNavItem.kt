package com.example.baristamessenger.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Build
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Chats : BottomNavItem("chats_tab", "Чаты", Icons.Default.Email)
    object Workspace : BottomNavItem("workspace_tab", "Биржа смен", Icons.Default.DateRange)
    object Tools : BottomNavItem("tools_tab", "Инструменты", Icons.Default.Build)
    object Profile : BottomNavItem("profile_tab", "Профиль", Icons.Default.Person)
}