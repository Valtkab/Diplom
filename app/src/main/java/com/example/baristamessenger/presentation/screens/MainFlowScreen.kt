package com.example.baristamessenger.presentation.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.baristamessenger.presentation.Screen
import com.example.baristamessenger.presentation.navigation.BottomNavItem
import androidx.navigation.NavController

@Composable
fun MainFlowScreen(
    currentUserId: String,
    navController: NavController, // Передаем навигатор, чтобы открывать экраны из списков
    onLogout: () -> Unit
) {
    // Храним состояние, какая вкладка сейчас выбрана
    var selectedTab by remember { mutableStateOf<BottomNavItem>(BottomNavItem.Chats) }

    Scaffold(
        bottomBar = {
            // Наша нижняя панель в темных кофейных тонах
            NavigationBar(
                containerColor = Color(0xFF1E1E1E),
                contentColor = Color.White
            ) {
                val items = listOf(
                    BottomNavItem.Chats,
                    BottomNavItem.Workspace,
                    BottomNavItem.Tools,
                    BottomNavItem.Profile
                )

                items.forEach { item ->
                    NavigationBarItem(
                        selected = selectedTab == item,
                        onClick = { selectedTab = item },
                        label = { Text(item.title, color = if (selectedTab == item) Color(0xFFD7CCC8) else Color.Gray) },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = if (selectedTab == item) Color(0xFFD7CCC8) else Color.Gray
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color(0xFF3E2723)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                // ИСПРАВЛЕНО: Теперь на первой вкладке отображается список чатов, а не пустой экран!
                is BottomNavItem.Chats -> {
                    ChatsListScreen(
                        onChatClick = { chatId ->
                            // При клике на чат из списка уходим на экран переписки
                            navController.navigate(Screen.Chat.createRoute(chatId))
                        },
                        onProfileClick = {
                            // Переключаемся на вкладку профиля
                            selectedTab = BottomNavItem.Profile
                        }
                    )
                }

                // 2 Вкладка: Биржа смен (пока заглушка)
                is BottomNavItem.Workspace -> {
                    WorkspaceScreen()
                }

                // 3 Вкладка: Инструменты (пока заглушка)
                is BottomNavItem.Tools -> {
                    CostCalculatorScreen()
                }

                // 4 Вкладка: Твой готовый ProfileScreen
                is BottomNavItem.Profile -> {
                    ProfileScreen(
                        onBackClick = { selectedTab = BottomNavItem.Chats },
                        onLogout = onLogout
                    )
                }
            }
        }
    }
}