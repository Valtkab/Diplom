package com.example.baristamessenger.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.baristamessenger.presentation.viewmodel.ChatsListViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsListScreen(
    onChatClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    viewModel: ChatsListViewModel = koinViewModel()
) {
    val chats by viewModel.chats.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var newChatName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Рабочие чаты",
                        color = Color(0xFFFFD700), // Золотой текст
                        fontWeight = FontWeight.SemiBold
                    )
                },
                // ИСПРАВЛЕНО: Плюсик переехал в верхний правый угол (туда, где красный кружок)
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Создать чат",
                            tint = Color(0xFFFFD700), // Золотой цвет иконки
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black // Чёрная шапка, как в чате
                )
            )
        },
        // ИСПРАВЛЕНО: Цвет фона всей страницы теперь тёмный, как в чате
        containerColor = Color(0xFF121212)
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(chats) { chat ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E1E1E)) // Тёмная подложка для элемента чата
                        .clickable { onChatClick(chat.id) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Аватарка-заглушка чата
                    Box(
                        modifier = Modifier
                            .size(45.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFD700).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = chat.name.take(1).uppercase(),
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = chat.name,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        if (chat.lastMessage.isNotEmpty()) {
                            Text(
                                text = chat.lastMessage,
                                color = Color.Gray,
                                fontSize = 14.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // Диалог создания чата (вызывается по нажатию на плюс в TopAppBar)
        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                containerColor = Color(0xFF333333),
                title = { Text("Создать новый чат", color = Color.White) },
                text = {
                    OutlinedTextField(
                        value = newChatName,
                        onValueChange = { newChatName = it },
                        label = { Text("Название чата") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFFD700),
                            focusedLabelColor = Color(0xFFFFD700),
                            unfocusedTextColor = Color.White,
                            focusedTextColor = Color.White
                        )
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (newChatName.isNotBlank()) {
                                viewModel.createChat(newChatName, isChannel = false)
                                newChatName = ""
                                showCreateDialog = false
                            }
                        }
                    ) {
                        Text("Создать", color = Color(0xFFFFD700))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false }) {
                        Text("Отмена", color = Color.Gray)
                    }
                }
            )
        }
    }
}