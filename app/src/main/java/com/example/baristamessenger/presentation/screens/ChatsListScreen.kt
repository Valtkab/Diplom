package com.example.baristamessenger.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.baristamessenger.domain.model.Chat
import com.example.baristamessenger.presentation.viewmodel.ChatsListViewModel
import org.koin.androidx.compose.koinViewModel
import androidx.compose.material.icons.filled.Person

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsListScreen(
    onChatClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    viewModel: ChatsListViewModel = koinViewModel()
) {
    val chats by viewModel.chats.collectAsState()

    // Состояние для управления показом диалогового окна
    var showDialog by remember { mutableStateOf(false) }
    // Текст внутри поля ввода в диалоговом окне
    var newChatName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Рабочие чаты", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4E342E), // Тёмно-кофейная шапка
                    titleContentColor = Color.White
                )
            )
        },

        // 1. Добавляем плавающую кнопку «+»
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true }, // При нажатии открываем диалог
                containerColor = Color(0xFF4E342E), // Кофейный стиль кнопки
                contentColor = Color.White
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Создать чат")
            }
        }
    ) { paddingValues ->

        // 2. Всплывающее диалоговое окно для создания нового чата
        if (showDialog) {
            AlertDialog(
                onDismissRequest = {
                    showDialog = false // Закрываем, если кликнули мимо окна
                    newChatName = "" // Очищаем поле
                },
                title = { Text("Создать новый чат") },
                text = {
                    Column {
                        Text("Введите название чата (например, название смены или темы обсуждения):")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newChatName,
                            onValueChange = { newChatName = it },
                            placeholder = { Text("Название чата...") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4E342E)),
                        onClick = {
                            if (newChatName.isNotBlank()) {
                                // Вызываем метод нашей ViewModel, который мы только что починили
                                viewModel.onCreateChatClick(newChatName)
                                showDialog = false
                                newChatName = "" // Очищаем поле
                            }
                        }
                    ) {
                        Text("Создать")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDialog = false
                            newChatName = ""
                        }
                    ) {
                        Text("Отмена", color = Color.Gray)
                    }
                }
            )
        }

        // Логика отображения списка чатов
        if (chats.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF4E342E))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(chats) { chat ->
                    ChatItem(chat = chat, onClick = { onChatClick(chat.id) })
                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                }
            }
        }
    }
}

@Composable
fun ChatItem(
    chat: Chat,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(48.dp).clip(CircleShape),
            color = Color(0xFFFFF3E0)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = chat.name.take(1),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE65100)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = chat.name,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = chat.lastMessage,
                fontSize = 14.sp,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}