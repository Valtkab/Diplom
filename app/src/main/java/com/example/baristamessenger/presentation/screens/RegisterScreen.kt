package com.example.baristamessenger.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.baristamessenger.presentation.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit,
    viewModel: AuthViewModel
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val registerState by viewModel.loginState.collectAsState()

    LaunchedEffect(registerState) {
        if (registerState is AuthViewModel.StateResult.Success) {
            onRegisterSuccess()
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
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Регистрация бариста",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4E342E)
            )

            Text(
                text = "Создайте рабочий аккаунт в системе",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            TextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("Имя") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            TextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Фамия") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            TextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = { Text("Никнейм (@nickname)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Логин (Рабочий Email)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Пароль (минимум 6 символов)") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (registerState is AuthViewModel.StateResult.Error) {
                Text(
                    text = (registerState as AuthViewModel.StateResult.Error).errorMessage,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            if (registerState is AuthViewModel.StateResult.Loading) {
                CircularProgressIndicator(color = Color(0xFF4E342E))
            } else {
                Button(
                    onClick = { viewModel.register(firstName, lastName, nickname, email, password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4E342E))
                ) {
                    Text("Создать аккаунт", fontSize = 16.sp, color = Color.White)
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = onBackToLogin) {
                    Text(
                        text = "Уза есть аккаунт? Войти",
                        color = Color(0xFF4E342E),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}