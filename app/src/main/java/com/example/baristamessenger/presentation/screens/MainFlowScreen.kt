package com.example.baristamessenger.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
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
import com.example.baristamessenger.presentation.viewmodel.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import org.koin.androidx.compose.koinViewModel

sealed class BottomBarItem(val route: String, val title: String, val icon: ImageVector) {
    object Orders : BottomBarItem("orders_line", "Заказы", Icons.Default.Home)
    object Chats : BottomBarItem("chats_list", "Чаты", Icons.AutoMirrored.Filled.Send)
    object Exchange : BottomBarItem("workspace", "Шифт-менеджмент", Icons.AutoMirrored.Filled.List)
    object Calculator : BottomBarItem("calculator", "Калькулятор", Icons.Default.Clear)
    object Profile : BottomBarItem("profile", "Профиль", Icons.Default.Person)
}

@Composable
fun MainFlowScreen(
    navController: NavController,
    currentUserId: String,
    currentUserName: String,
    onLogout: () -> Unit = {}
) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val profileViewModel: ProfileViewModel = koinViewModel()

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.Black, contentColor = Color.White) {
                listOf(BottomBarItem.Orders, BottomBarItem.Chats, BottomBarItem.Exchange, BottomBarItem.Profile).forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title, fontSize = 12.sp) },
                        selected = currentRoute == item.route,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFFFD700),
                            unselectedIconColor = Color.Gray,
                            indicatorColor = Color.Transparent
                        ),
                        onClick = {
                            if (currentRoute != item.route) {
                                bottomNavController.navigate(item.route) {
                                    popUpTo(BottomBarItem.Orders.route) { saveState = true }
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
            startDestination = BottomBarItem.Orders.route,
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            composable(BottomBarItem.Orders.route) { OrderScreen() }
            composable(BottomBarItem.Chats.route) {
                ChatsListScreen(
                    onChatClick = { chatId -> navController.navigate(Screen.Chat.createRoute(chatId)) },
                    onProfileClick = { bottomNavController.navigate(BottomBarItem.Profile.route) }
                )
            }
            composable(BottomBarItem.Exchange.route) {
                WorkspaceScreen(
                    onBackClick = { bottomNavController.popBackStack() },
                    currentUserName = currentUserName
                )
            }
            composable(BottomBarItem.Profile.route) {
                ProfileScreen(viewModel = profileViewModel, onBackClick = { bottomNavController.popBackStack() }, onLogout = onLogout)
            }
        }
    }
}

@Composable
fun ManagerWorkspaceScreen(navController: NavController, onLogout: () -> Unit = {}) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val profileViewModel: ProfileViewModel = koinViewModel()
    val currentUserName = FirebaseAuth.getInstance().currentUser?.displayName ?: "Управляющий"

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.Black) {
                listOf(BottomBarItem.Chats, BottomBarItem.Calculator, BottomBarItem.Exchange, BottomBarItem.Profile).forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title, fontSize = 12.sp) },
                        selected = currentRoute == item.route,
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFFFFD700), unselectedIconColor = Color.Gray),
                        onClick = {
                            if (currentRoute != item.route) {
                                bottomNavController.navigate(item.route) {
                                    popUpTo(BottomBarItem.Chats.route) { saveState = true }
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
        NavHost(bottomNavController, startDestination = BottomBarItem.Chats.route, modifier = Modifier.padding(paddingValues)) {
            composable(BottomBarItem.Chats.route) { ChatsListScreen({ navController.navigate(Screen.Chat.createRoute(it)) }, { bottomNavController.navigate(BottomBarItem.Profile.route) }) }
            composable(BottomBarItem.Calculator.route) { CostCalculatorScreen { bottomNavController.popBackStack() } }
            composable(BottomBarItem.Exchange.route) { WorkspaceScreen({ bottomNavController.popBackStack() }, currentUserName) }
            composable(BottomBarItem.Profile.route) { ProfileScreen(profileViewModel, { bottomNavController.popBackStack() }, onLogout) }
        }
    }
}