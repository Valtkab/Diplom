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

    var showUserPicker by remember { mutableStateOf(false) }
    var showGroupDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Рабочие чаты",
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {

                    // 🔥 КНОПКА СОЗДАНИЯ ЛИЧНОГО ЧАТА
                    IconButton(onClick = {
                        showUserPicker = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Личный чат",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // 🔥 КНОПКА СОЗДАНИЯ ГРУППЫ
                    IconButton(onClick = {
                        showGroupDialog = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Группа",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black
                )
            )
        },
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
                        .background(Color(0xFF1E1E1E))
                        .clickable { onChatClick(chat.id) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

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

        // 🔥 ЛИЧНЫЙ ЧАТ
        if (showUserPicker) {
            UserPickerDialog(
                onDismiss = {
                    showUserPicker = false
                },
                onUserSelected = { user ->
                    viewModel.createPrivateChat(
                        user.id,
                        user.name
                    )
                    showUserPicker = false
                }
            )
        }

        // 🔥 ГРУППОВОЙ ЧАТ
        if (showGroupDialog) {
            GroupCreateDialog(
                onDismiss = {
                    showGroupDialog = false
                },
                onCreate = { name, users ->

                    viewModel.createGroupChat(
                        name,
                        users
                    )

                    showGroupDialog = false
                }
            )
        }
    }
}