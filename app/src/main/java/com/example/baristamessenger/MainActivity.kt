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
import com.example.baristamessenger.presentation.screens.*
import com.example.baristamessenger.presentation.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import org.koin.androidx.compose.koinViewModel
import androidx.core.view.WindowCompat


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ИСПРАВЛЕНО: Перенесли сюда (до setContent).
        // Теперь Android официально разрешает элементам плавно реагировать на клавиатуру.
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val navController = rememberNavController()
            val authViewModel: AuthViewModel = koinViewModel()

            NavHost(navController = navController, startDestination = Screen.Login.route) {

                composable(Screen.Login.route) {
                    LoginScreen(
                        onLoginSuccess = {
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

                composable(Screen.Register.route) {
                    RegisterScreen(
                        onRegisterSuccess = {
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

                // Глобальный экран переписки
                composable(
                    route = Screen.Chat.route,
                    arguments = listOf(navArgument("chatId") { type = NavType.StringType })
                ) { backStackEntry ->
                    ChatScreen(
                        chatId = backStackEntry.arguments?.getString("chatId") ?: "",
                        onBackClick = { navController.popBackStack() }
                    )
                }

                // Главный поток приложения (внутри него живет нижнее меню)
                composable("main_flow") {
                    MainFlowScreen(
                        currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                        navController = navController,
                        onLogout = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo("main_flow") { inclusive = true }
                            }
                        }
                    )
                }

                // Внутренние экраны фич (открываются поверх нижней панели)
                composable(Screen.Calculator.route) {
                    CostCalculatorScreen(onBackClick = { navController.popBackStack() })
                } 

                // ИСПРАВЛЕНО: Теперь при переходе на Биржу откроется твой экран из WorkspaceScreen!
                composable(Screen.Exchange.route) {
                    WorkspaceScreen(onBackClick = { navController.popBackStack() })
                }
            }
        }
    }
}