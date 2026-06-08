package com.example.baristamessenger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.baristamessenger.presentation.Screen
import com.example.baristamessenger.presentation.screens.*
import com.example.baristamessenger.presentation.viewmodel.AuthViewModel
import com.example.baristamessenger.presentation.viewmodel.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import org.koin.androidx.compose.koinViewModel
import androidx.core.view.WindowCompat
import androidx.compose.runtime.collectAsState
import com.example.baristamessenger.data.CloudinaryManager
import androidx.navigation.NavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    // Если сессия активна, отправляем на splash для проверки роли, иначе — на логин
    val startScreen = if (currentUser != null) "splash" else Screen.Login.route
    val userName = currentUser?.displayName ?: "Бариста"

    NavHost(
        navController = navController,
        startDestination = startScreen
    ) {

        // ЭКРАН-РАСПРЕДЕЛИТЕЛЬ РОЛЕЙ
        composable("splash") {
            val profileViewModel: ProfileViewModel = koinViewModel()
            val userProfile by profileViewModel.userProfile.collectAsState()

            LaunchedEffect(userProfile) {
                userProfile?.let { user ->
                    // Приводим к строке и верхнему регистру для защиты от опечаток
                    val role = user.role.toString().uppercase()
                    if (role == "MANAGER") {
                        navController.navigate("manager_flow") {
                            popUpTo("splash") { inclusive = true }
                        }
                    } else {
                        navController.navigate("main_flow") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                }
            }

            // Индикатор загрузки, пока Firebase отдает профиль с ролью
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("splash") {
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

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("splash") {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onBackToLogin = {
                    navController.popBackStack()
                },
                viewModel = authViewModel
            )
        }

        composable(
            route = Screen.Chat.route,
            arguments = listOf(navArgument("chatId") { type = NavType.StringType })
        ) { backStackEntry ->
            ChatScreen(
                chatId = backStackEntry.arguments?.getString("chatId") ?: "",
                onBackClick = { navController.popBackStack() }
            )
        }

        // ПОТОК ДЛЯ БАРИСТА (Твое текущее основное приложение)
        composable("main_flow") {
            MainFlowScreen(
                currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                navController = navController,
                currentUserName = userName,
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo("main_flow") { inclusive = true }
                    }
                }
            )
        }

        // ПОТОК ДЛЯ УПРАВЛЯЮЩЕГО
        composable("manager_flow") {
            ManagerWorkspaceScreen(
                navController = navController,
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo("manager_flow") { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Profile.route) {
            val profileViewModel: ProfileViewModel = koinViewModel()
            ProfileScreen(
                viewModel = profileViewModel,
                onBackClick = { navController.popBackStack() },
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo("main_flow") { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Calculator.route) {
            CostCalculatorScreen(onBackClick = { navController.popBackStack() })
        }

        composable(Screen.Exchange.route) {
            WorkspaceScreen(onBackClick = { navController.popBackStack() })
        }
    }
}

// ЭКРАН-ЗАГЛУШКА ДЛЯ УПРАВЛЯЮЩЕГО (С кнопками перехода в твои калькулятор и биржу)

