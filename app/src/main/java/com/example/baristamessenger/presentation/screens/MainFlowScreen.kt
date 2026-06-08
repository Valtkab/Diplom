package com.example.baristamessenger.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

// Все возможные вкладки в системе
sealed class BottomBarItem(val route: String, val title: String, val icon: ImageVector) {
    object Orders : BottomBarItem("orders_line", "Заказы", Icons.Default.Home)
    object Chats : BottomBarItem("chats_list", "Чаты", Icons.AutoMirrored.Filled.Send)
    object Exchange : BottomBarItem("workspace", "Биржа", Icons.AutoMirrored.Filled.List) // Для бариста это биржа смен!
    object Calculator : BottomBarItem("calculator", "Калькулятор", Icons.Default.Clear)
    object Profile : BottomBarItem("profile", "Профиль", Icons.Default.Person)
}

// =======================================================================
// 1. ПОТОК БАРИСТА (Вкладки: Заказы, Чаты, Биржа смен, Профиль)
// =======================================================================
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

    // ВЕРНУЛИ НА МЕСТО: Бариста снова видит Биржу смен (Exchange)
    val baristaItems = listOf(
        BottomBarItem.Orders,
        BottomBarItem.Chats,
        BottomBarItem.Exchange,
        BottomBarItem.Profile
    )

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.Black, contentColor = Color.White) {
                baristaItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title, fontSize = 12.sp) },
                        selected = currentRoute == item.route,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFFFD700),
                            selectedTextColor = Color(0xFFFFD700),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
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
            composable(BottomBarItem.Orders.route) {
                BaristaOrdersLineScreen()
            }

            composable(BottomBarItem.Chats.route) {
                ChatsListScreen(
                    onChatClick = { chatId -> navController.navigate(Screen.Chat.createRoute(chatId)) },
                    onProfileClick = { bottomNavController.navigate(BottomBarItem.Profile.route) }
                )
            }

            // ВЕРНУЛИ НА МЕСТО: Экран биржи смен для бариста внутри его навигации
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

// =======================================================================
// 2. ПОТОК УПРАВЛЯЮЩЕГО (Вкладки: Чаты, Калькулятор, Биржа/Запасы, Профиль)
// =======================================================================
@Composable
fun ManagerWorkspaceScreen(
    navController: NavController,
    onLogout: () -> Unit = {}
) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val profileViewModel: ProfileViewModel = koinViewModel()

    val currentUserName = FirebaseAuth.getInstance().currentUser?.displayName ?: "Управляющий"

    val managerItems = listOf(
        BottomBarItem.Chats,
        BottomBarItem.Calculator,
        BottomBarItem.Exchange,
        BottomBarItem.Profile
    )

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.Black, contentColor = Color.White) {
                managerItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title, fontSize = 12.sp) },
                        selected = currentRoute == item.route,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFFFD700),
                            selectedTextColor = Color(0xFFFFD700),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color.Transparent
                        ),
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
        NavHost(
            navController = bottomNavController,
            startDestination = BottomBarItem.Chats.route,
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            composable(BottomBarItem.Chats.route) {
                ChatsListScreen(
                    onChatClick = { chatId -> navController.navigate(Screen.Chat.createRoute(chatId)) },
                    onProfileClick = { bottomNavController.navigate(BottomBarItem.Profile.route) }
                )
            }
            composable(BottomBarItem.Calculator.route) {
                CostCalculatorScreen(onBackClick = { bottomNavController.popBackStack() })
            }
            composable(BottomBarItem.Exchange.route) {
                WorkspaceScreen(onBackClick = { bottomNavController.popBackStack() }, currentUserName = currentUserName)
            }
            composable(BottomBarItem.Profile.route) {
                ProfileScreen(viewModel = profileViewModel, onBackClick = { bottomNavController.popBackStack() }, onLogout = onLogout)
            }
        }
    }
}

// =======================================================================
// Вспомогательный UI-компонент: ЛИНИЯ ЗАКАЗОВ ДЛЯ БАРИСТА
// =======================================================================
data class OrderMock(val id: Int, val table: String, val items: String)

@Composable
fun BaristaOrdersLineScreen() {
    val activeOrders = remember {
        mutableStateListOf(
            OrderMock(104, "Столик №3", "1х Капучино (Кокосовое), 1х Круассан"),
            OrderMock(105, "На вынос", "2х Латте макиато (Синнамон)"),
            OrderMock(106, "Столик №1", "1х Эспрессо, 1х Чизкейк")
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text(
            text = "Линия заказов (Поток)",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (activeOrders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Все заказы выполнены! 🎉", color = Color.Gray, fontSize = 16.sp)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(activeOrders) { order ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Заказ #${order.id} — ${order.table}", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = order.items, color = Color.White, fontSize = 14.sp)
                            }
                            IconButton(
                                onClick = { activeOrders.remove(order) },
                                colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFF2E7D32))
                            ) {
                                Icon(Icons.Default.Check, contentDescription = "Готово", tint = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}