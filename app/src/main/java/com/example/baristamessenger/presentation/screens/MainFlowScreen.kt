package com.example.baristamessenger.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.baristamessenger.presentation.Screen

sealed class BottomNavItem(val screen: Screen, val title: String, val icon: ImageVector) {
    object Chats : BottomNavItem(Screen.ChatsList, "Чаты", Icons.Default.MailOutline)
    // Инструменты теперь просто кнопка-триггер, а не отдельный экран
    object Tools : BottomNavItem(Screen.ToolsHub, "Инструменты", Icons.Default.Build)
    object Notifications : BottomNavItem(Screen.Notifications, "Уведомления", Icons.Default.Notifications)
    object Profile : BottomNavItem(Screen.Profile, "Профиль", Icons.Default.Person)
}

@Composable
fun MainFlowScreen(
    currentUserId: String,
    navController: NavController, // Глобальный навигатор (Main)
    onLogout: () -> Unit
) {
    val bottomNavController = rememberNavController()
    // Состояние для открытия/закрытия стеклянного меню инструментов
    var isToolsMenuExpanded by remember { mutableStateOf(false) }

    val items = listOf(
        BottomNavItem.Chats,
        BottomNavItem.Tools,
        BottomNavItem.Notifications,
        BottomNavItem.Profile
    )

    // Оборачиваем всё в Box, чтобы меню вылезало ПОВЕРХ Scaffold'а
    Box(modifier = Modifier.fillMaxSize()) {

        // Главный каркас приложения
        Scaffold(
            // Если меню открыто — размываем весь задний фон
            modifier = Modifier.blur(if (isToolsMenuExpanded) 15.dp else 0.dp),
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    items.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            selected = currentRoute == item.screen.route && !isToolsMenuExpanded,
                            onClick = {
                                if (item.screen == Screen.ToolsHub) {
                                    // При клике на "Инструменты" просто открываем оверлей (НЕ переходим)
                                    isToolsMenuExpanded = !isToolsMenuExpanded
                                } else {
                                    // При клике на другие табы - закрываем оверлей и переходим
                                    isToolsMenuExpanded = false
                                    if (currentRoute != item.screen.route) {
                                        bottomNavController.navigate(item.screen.route) {
                                            popUpTo(bottomNavController.graph.startDestinationId) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = bottomNavController,
                startDestination = BottomNavItem.Chats.screen.route,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(Screen.ChatsList.route) {
                    ChatsListScreen(
                        onChatClick = { chatId ->
                            navController.navigate(
                                Screen.Chat.createRoute(
                                    chatId
                                )
                            )
                        },
                        onProfileClick = { bottomNavController.navigate(Screen.Profile.route) }
                    )
                }

                composable(Screen.Notifications.route) {
                    Surface { Text("Экран уведомлений") }
                }

                composable(Screen.Profile.route) {
                    ProfileScreen(
                        onBackClick = { bottomNavController.popBackStack() },
                        onLogout = onLogout
                    )
                }
                // Заметь: Screen.ToolsHub тут больше нет, так как это теперь всплывающее меню!
            }
        }

        // ВСПЛЫВАЮЩЕЕ МЕНЮ С ЭФФЕКТОМ СТЕКЛА (Оверлей)
        AnimatedVisibility(
            visible = isToolsMenuExpanded,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    // Полупрозрачный затемненный фон для эффекта стекла
                    .background(Color.Black.copy(alpha = 0.5f))
                    // Закрываем меню при клике в пустую область
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { isToolsMenuExpanded = false },
                contentAlignment = Alignment.BottomCenter
            ) {
                // Панель с 4 круглыми кнопками (Используем только 100% Core-иконки)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 120.dp, start = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    ToolRoundButton(
                        icon = Icons.Default.Share, // ИСПРАВЛЕНО: Есть в Core (вместо Refresh)
                        label = "Биржа",
                        onClick = {
                            isToolsMenuExpanded = false
                            navController.navigate(Screen.Exchange.route)
                        }
                    )
                    ToolRoundButton(
                        icon = Icons.Default.Add, // ИСПРАВЛЕНО: Есть в Core (обычный плюсик для расчетов)
                        label = "Калькулятор",
                        onClick = {
                            isToolsMenuExpanded = false
                            navController.navigate(Screen.Calculator.route)
                        }
                    )
                    ToolRoundButton(
                        icon = Icons.Default.ShoppingCart, // Есть в Core
                        label = "Барахолка",
                        onClick = {
                            isToolsMenuExpanded = false
                            navController.navigate(Screen.Market.route)
                        }
                    )
                    ToolRoundButton(
                        icon = Icons.Default.Build, // Есть в Core
                        label = "SOS",
                        onClick = {
                            isToolsMenuExpanded = false
                            navController.navigate(Screen.Help.route)
                        }
                    )
                }
            }
        }
    }
}

// Компонент круглой кнопки для всплывающего меню
@Composable
fun ToolRoundButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color(0xFF382C24)), // Темно-кофейный цвет кнопки
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFFF1D3B3), // Светло-бежевая иконка
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}