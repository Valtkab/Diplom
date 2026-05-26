package com.example.baristamessenger.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.baristamessenger.presentation.viewmodel.ChatViewModel
import com.google.firebase.auth.FirebaseAuth
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatId: String,
    onBackClick: () -> Unit,
    viewModel: ChatViewModel = koinViewModel()
) {
    LaunchedEffect(chatId) {
        viewModel.loadMessages(chatId)
    }

    val messages by viewModel.messages.collectAsState()
    var inputText by remember { mutableStateOf("") }

    // Получаем реальный ID текущего авторизованного бариста
    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Чат кофейни ☕") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .navigationBarsPadding()
                .imePadding()
        ) {

            // 1. СПИСОК СООБЩЕНИЙ (Разделение по разным сторонам экрана)
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                reverseLayout = true,
                verticalArrangement = Arrangement.Bottom
            ) {
                items(messages.reversed()) { message ->
                    // Сравниваем с реальным UID из Firebase
                    val isCurrentUser = message.senderId == currentUserId

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        // Твои сообщения летят вправо, чужие — влево
                        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
                    ) {
                        Column(
                            modifier = Modifier.widthIn(max = 280.dp),
                            horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
                        ) {
                            // Отображаем имя автора над сообщениями других бариста
                            if (!isCurrentUser) {
                                Text(
                                    text = message.senderName.ifEmpty { "Бариста" },
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                                )
                            }

                            // Красивое облачко (баббл) сообщения
                            Surface(
                                // Скругляем углы как в Telegram
                                shape = RoundedCornerShape(
                                    topStart = 12.dp,
                                    topEnd = 12.dp,
                                    bottomStart = if (isCurrentUser) 12.dp else 0.dp,
                                    bottomEnd = if (isCurrentUser) 0.dp else 12.dp
                                ),
                                // Твои сообщения — кофейные, чужие — серые
                                color = if (isCurrentUser) Color(0xFFD7CCC8) else Color(0xFFF5F5F5),
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = message.text,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    fontSize = 16.sp,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }

            // 2. НИЖНЯЯ ПАНЕЛЬ ВВОДА
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                )

                Spacer(modifier = Modifier.width(4.dp))

                Button(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendMessage(chatId, inputText)
                            inputText = ""
                        }
                    }
                ) {
                    Text("Отпр.")
                }

                Spacer(modifier = Modifier.width(4.dp))

                Button(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendMessage(chatId, inputText)
                            inputText = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Text("ТТК")
                }
            }
        }
    }
}