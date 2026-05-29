package com.example.baristamessenger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.baristamessenger.presentation.Screen
import com.example.baristamessenger.presentation.screens.ChatScreen
import com.example.baristamessenger.presentation.screens.ChatsListScreen
import com.example.baristamessenger.presentation.screens.LoginScreen
import com.example.baristamessenger.presentation.screens.MainFlowScreen
import com.example.baristamessenger.presentation.screens.ProfileScreen
import com.example.baristamessenger.presentation.screens.RegisterScreen
import com.example.baristamessenger.presentation.viewmodel.AuthViewModel
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val authViewModel: AuthViewModel = koinViewModel()

            NavHost(navController = navController, startDestination = Screen.Login.route) {

                // 1. Экран авторизации (Вход)
                composable(Screen.Login.route) {
                    LoginScreen(
                        onLoginSuccess = {
                            // ИСПРАВЛЕНО: Переходим на главный экран с нижней навигацией
                            navController.navigate("main_flow") {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        },
                        onRegisterClick = {
                            navController.navigate(Screen.Register.route)
                        },
                        viewModel = authViewModel
                    )
                }

                // 2. Экран регистрации
                composable(Screen.Register.route) {
                    RegisterScreen(
                        onRegisterSuccess = {
                            // ИСПРАВЛЕНО: После регистрации тоже переходим в главное меню
                            navController.navigate("main_flow") {
                                popUpTo(Screen.Register.route) { inclusive = true }
                            }
                        },
                        onBackToLogin = {
                            navController.popBackStack()
                        },
                        viewModel = authViewModel
                    )
                }

                // 3. Экран списка рабочих чатов
                composable(Screen.ChatsList.route) {
                    ChatsListScreen(
                        onChatClick = { chatId ->
                            navController.navigate(Screen.Chat.createRoute(chatId))
                        },
                        onProfileClick = {
                            navController.navigate(Screen.Profile.route)
                        }
                    )
                }

                // 4. Экран самого чата (переписки)
                composable(
                    route = Screen.Chat.route,
                    arguments = listOf(navArgument("chatId") { type = NavType.StringType })
                ) { backStackEntry ->
                    ChatScreen(
                        chatId = backStackEntry.arguments?.getString("chatId") ?: "",
                        onBackClick = { navController.popBackStack() }
                    )
                }

                // 5. Экран профиля бариста
                composable(Screen.Profile.route) {
                    ProfileScreen(
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onLogout = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.ChatsList.route) { inclusive = true }
                            }
                        }
                    )
                }

                // 6. ИСПРАВЛЕНО: Новый главный экран с нижней панелью навигации (вынесен отдельно)
                // Внутри NavHost в MainActivity.kt
                composable("main_flow") {
                    MainFlowScreen(
                        currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "",
                        navController = navController, // ИСПРАВЛЕНО: Передаем навигатор внутрь главного экрана
                        onLogout = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo("main_flow") { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}