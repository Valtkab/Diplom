package com.example.baristamessenger.presentation.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.baristamessenger.domain.model.Chat
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

    var showDialog by remember { mutableStateOf(false) }
    var newChatName by remember { mutableStateOf("") }
    var isCreatingChannel by remember { mutableStateOf(false) }

    var selectedTabIndex by remember { mutableStateOf(0) }
    var chatToDelete by remember { mutableStateOf<Chat?>(null) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Рабочие чаты", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF4E342E),
                        titleContentColor = Color.White
                    )
                )
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color(0xFF4E342E),
                    contentColor = Color.White
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text("Чаты", color = if (selectedTabIndex == 0) Color.White else Color.Gray) }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = { Text("Каналы", color = if (selectedTabIndex == 1) Color.White else Color.Gray) }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = Color(0xFF4E342E),
                contentColor = Color.White
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Создать")
            }
        }
    ) { paddingValues ->

        if (showDialog) {
            AlertDialog(
                onDismissRequest = {
                    showDialog = false
                    newChatName = ""
                    isCreatingChannel = false
                },
                title = { Text(if (isCreatingChannel) "Создать новый канал" else "Создать новый чат") },
                text = {
                    Column {
                        Text("Введите название:")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newChatName,
                            onValueChange = { newChatName = it },
                            placeholder = { Text("Название...") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isCreatingChannel,
                                onCheckedChange = { isCreatingChannel = it },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF4E342E))
                            )
                            Text("Это публичный канал (посты)")
                        }
                    }
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4E342E)),
                        onClick = {
                            if (newChatName.isNotBlank()) {
                                // ПРИМЕЧАНИЕ: Передаем флаг канала, если твоя ViewModel это поддерживает
                                viewModel.onCreateChatClick(newChatName)
                                showDialog = false
                                newChatName = ""
                                isCreatingChannel = false
                            }
                        }
                    ) {
                        Text("Создать")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false; newChatName = ""; isCreatingChannel = false }) {
                        Text("Отмена", color = Color.Gray)
                    }
                }
            )
        }

        if (chatToDelete != null) {
            AlertDialog(
                onDismissRequest = { chatToDelete = null },
                title = { Text("Удалить ${chatToDelete?.name}?") },
                text = { Text("Это действие удалит переписку у всех. Отменить нельзя.") },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        onClick = {
                            // ВЫЗОВ УДАЛЕНИЯ ЧАТА ВО ВЬЮМОДЕЛИ
                            viewModel.deleteChat(chatToDelete!!.id)
                            chatToDelete = null
                        }
                    ) {
                        Text("Удалить")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { chatToDelete = null }) {
                        Text("Отмена", color = Color.Gray)
                    }
                }
            )
        }

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
                // Фильтруем список в зависимости от вкладки
                val displayedChats = chats.filter { it.isChannel == (selectedTabIndex == 1) }

                items(displayedChats) { chat ->
                    ChatItem(
                        chat = chat,
                        onClick = { onChatClick(chat.id) },
                        onLongClick = { chatToDelete = chat }
                    )
                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatItem(
    chat: Chat,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ВИЗУАЛЬНОЕ ОТЛИЧИЕ КАНАЛОВ ОТ ЧАТОВ
        val shape = if (chat.isChannel) RoundedCornerShape(12.dp) else CircleShape
        val avatarColor = if (chat.isChannel) Color(0xFFD1C4E9) else Color(0xFFFFF3E0)
        val textColor = if (chat.isChannel) Color(0xFF4527A0) else Color(0xFFE65100)

        Surface(
            modifier = Modifier.size(52.dp).clip(shape),
            color = avatarColor
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = chat.name.take(1).uppercase(),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = chat.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                if (chat.isChannel) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = Color(0xFFE8EAF6),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "КАНАЛ",
                            fontSize = 10.sp,
                            color = Color(0xFF3F51B5),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }
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