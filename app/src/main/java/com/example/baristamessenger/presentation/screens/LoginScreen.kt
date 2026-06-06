package com.example.baristamessenger.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.baristamessenger.presentation.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    navController: NavController,
    viewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val loginState by viewModel.loginState.collectAsState()

    // Слушаем состояние Firebase. Как только вход успешен — вызываем переход на главный экран
    LaunchedEffect(loginState) {
        if (loginState is AuthViewModel.StateResult.Success) {
            onLoginSuccess()
            viewModel.resetState()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Coffix",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4E342E)
            )

            Text(
                text = "Вход в рабочую экосистему",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Рабочий Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Пароль") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Отображение ошибки от Firebase (например, неверный пароль)
            if (loginState is AuthViewModel.StateResult.Error) {
                Text(
                    text = (loginState as AuthViewModel.StateResult.Error).errorMessage,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Показываем индикатор загрузки, пока Firebase проверяет данные
            if (loginState is AuthViewModel.StateResult.Loading) {
                CircularProgressIndicator(color = Color(0xFF4E342E))
            } else {
                Button(
                    onClick = { viewModel.login(email, password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4E342E))
                ) {
                    Text("Войти в смену", fontSize = 16.sp, color = Color.White)
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = onRegisterClick) {
                    Text(
                        text = "Ещё не в смене? Зарегистрироваться",
                        color = Color(0xFF4E342E),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}