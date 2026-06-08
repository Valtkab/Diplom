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
import com.example.baristamessenger.domain.model.UserRole
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

    // 🔥 Состояние для выбора роли
    var selectedRole by remember { mutableStateOf(UserRole.BARISTA) }

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
                text = "Регистрация",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4E342E)
            )

            // --- БЛОК ВЫБОРА РОЛИ ---
            Text(
                text = "Кто вы?",
                fontSize = 16.sp,
                color = Color(0xFF4E342E),
                modifier = Modifier.padding(top = 16.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { selectedRole = UserRole.BARISTA },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (selectedRole == UserRole.BARISTA) Color(0xFF4E342E) else Color.Transparent
                    )
                ) {
                    Text("Бариста", color = if (selectedRole == UserRole.BARISTA) Color.White else Color(0xFF4E342E))
                }
                OutlinedButton(
                    onClick = { selectedRole = UserRole.MANAGER },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (selectedRole == UserRole.MANAGER) Color(0xFF4E342E) else Color.Transparent
                    )
                ) {
                    Text("Управляющий", color = if (selectedRole == UserRole.MANAGER) Color.White else Color(0xFF4E342E))
                }
            }
            // ------------------------

            TextField(value = firstName, onValueChange = { firstName = it }, label = { Text("Имя") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(modifier = Modifier.height(12.dp))
            TextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Фамилия") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(modifier = Modifier.height(12.dp))
            TextField(value = nickname, onValueChange = { nickname = it }, label = { Text("Никнейм") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(modifier = Modifier.height(12.dp))
            TextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), singleLine = true)
            Spacer(modifier = Modifier.height(12.dp))
            TextField(value = password, onValueChange = { password = it }, label = { Text("Пароль") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation(), singleLine = true)
            Spacer(modifier = Modifier.height(24.dp))

            if (registerState is AuthViewModel.StateResult.Error) {
                Text(text = (registerState as AuthViewModel.StateResult.Error).errorMessage, color = Color.Red, fontSize = 14.sp, modifier = Modifier.padding(bottom = 16.dp))
            }

            if (registerState is AuthViewModel.StateResult.Loading) {
                CircularProgressIndicator(color = Color(0xFF4E342E))
            } else {
                Button(
                    onClick = { viewModel.register(firstName, lastName, nickname, email, password, selectedRole) }, // 🔥 Передаем роль
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4E342E))
                ) {
                    Text("Создать аккаунт", fontSize = 16.sp, color = Color.White)
                }
                TextButton(onClick = onBackToLogin) {
                    Text("Уже есть аккаунт? Войти", color = Color(0xFF4E342E), fontSize = 14.sp)
                }
            }
        }
    }
}