package com.example.baristamessenger.presentation.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.baristamessenger.presentation.Screen


// Вспомогательный класс для вкладок нижней панели
sealed class BottomBarItem(val route: String, val title: String, val icon: ImageVector) {
    object Chats : BottomBarItem("chats_list", "Чаты", Icons.Default.Send)
    object Exchange : BottomBarItem("workspace", "Биржа", Icons.Default.List)
    object Calculator : BottomBarItem("calculator", "Калькулятор", Icons.Default.Clear)
    object Profile : BottomBarItem("profile", "Профиль", Icons.Default.Person)
}

@Composable
fun MainFlowScreen(
    navController: NavController,
    currentUserId: String,
    currentUserName: String, // 👈 ИСПРАВЛЕНО: Лишний параметр avController удален
    onLogout: () -> Unit = {}
) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = listOf(
        BottomBarItem.Chats,
        BottomBarItem.Exchange,
        BottomBarItem.Calculator,
        BottomBarItem.Profile
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.Black,
                contentColor = Color.White
            ) {
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title, fontSize = 12.sp) },
                        selected = currentRoute == item.route,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFFFD700), // Красивый золотой цвет
                            selectedTextColor = Color(0xFFFFD700),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color.Transparent
                        ),
                        onClick = {
                            if (currentRoute != item.route) {
                                bottomNavController.navigate(item.route) {
                                    popUpTo(BottomBarItem.Chats.route) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        },
        containerColor = Color(0xFF121212)
    ) { paddingValues ->

        NavHost(
            navController = bottomNavController,
            startDestination = BottomBarItem.Chats.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 1. ЭКРАН РАБОЧИХ ЧАТОВ
            composable(BottomBarItem.Chats.route) {
                ChatsListScreen(
                    onChatClick = { chatId ->
                        navController.navigate(Screen.Chat.createRoute(chatId))
                    },
                    onProfileClick = {
                        bottomNavController.navigate(BottomBarItem.Profile.route)
                    }
                )
            }

            // 2. ЭКРАН БИРЖИ СМЕН (Workspace)
            composable(BottomBarItem.Exchange.route) {
                WorkspaceScreen(
                    onBackClick = { navController.popBackStack() },
                    currentUserName = currentUserName // Передаем никнейм дальше в WorkspaceScreen
                )
            }

            // 3. ЭКРАН КАЛЬКУЛЯТОРА
            composable(BottomBarItem.Calculator.route) {
                CostCalculatorScreen(onBackClick = { bottomNavController.popBackStack() })
            }

            // 4. ЭКРАН ПРОФИЛЯ
            composable(BottomBarItem.Profile.route) {
                ProfileScreen(
                    onBackClick = { bottomNavController.popBackStack() },
                    onLogout = onLogout
                )
            }
        }
    }
}