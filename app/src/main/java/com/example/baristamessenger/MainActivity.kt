package com.example.baristamessenger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
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
import com.example.baristamessenger.data.CloudinaryManager



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Теперь Android официально разрешает элементам плавно реагировать на клавиатуру.
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            BaristaAppNavigation()
            CloudinaryManager.init(this)
        }
    }
}

@Composable
fun BaristaAppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = koinViewModel()

    // 1. Получаем экземпляр Firebase Auth
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    // 2. Если currentUser не null, значит сессия активна — сразу пускаем в приложение
    val startScreen = if (currentUser != null) "main_flow" else Screen.Login.route

    // 3. Берем имя пользователя из Firebase (если оно там сохранено) или используем дефолт
    val userName = currentUser?.displayName ?: "Бариста"

    NavHost(
        navController = navController,
        startDestination = startScreen
    ) {
        // Экран логина
        // Экран логина
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
                viewModel = authViewModel,
                navController = navController
            )
        }

        // Экран регистрация
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
                navController = navController, // Передаем ровно navController
                currentUserName = userName,    // Передаем имя пользователя в поток
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
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

        // При переходе на Биржу откроется твой чистый экран из WorkspaceScreen!
        composable(Screen.Exchange.route) {
            WorkspaceScreen(onBackClick = { navController.popBackStack() })
        }
    }
}