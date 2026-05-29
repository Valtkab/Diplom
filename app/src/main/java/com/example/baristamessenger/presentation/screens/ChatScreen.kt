package com.example.baristamessenger.presentation.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add // ИСПРАВЛЕНО: Безопасная стандартная иконка Плюса (+)
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
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
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Лаунчер для открытия галереи устройства
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Чат кофейни ☕", color = Color.White, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E))
            )
        },
        containerColor = Color(0xFF121212)
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .navigationBarsPadding()
                .imePadding()
        ) {

            // 1. СПИСОК СООБЩЕНИЙ
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                reverseLayout = true,
                verticalArrangement = Arrangement.Bottom
            ) {
                items(messages.reversed()) { message ->
                    val isCurrentUser = message.senderId == currentUserId

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
                    ) {
                        Column(
                            modifier = Modifier.widthIn(max = 280.dp),
                            horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
                        ) {
                            if (!isCurrentUser) {
                                Text(
                                    text = message.senderName.ifEmpty { "Бариста" },
                                    fontSize = 12.sp,
                                    color = Color(0xFFD7CCC8),
                                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(
                                    topStart = 12.dp,
                                    topEnd = 12.dp,
                                    bottomStart = if (isCurrentUser) 12.dp else 0.dp,
                                    bottomEnd = if (isCurrentUser) 0.dp else 12.dp
                                ),
                                color = if (isCurrentUser) Color(0xFF5D4037) else Color(0xFF1E1E1E),
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(6.dp)) {

                                    // Рендеринг картинки, если строка содержитUri-путь
                                    if (message.text.contains("content://") || message.text.contains("http")) {
                                        val uriString = message.text.substringAfter("📸описание:").trim()
                                        val textPart = message.text.substringBefore("📸описание:").trim()

                                        AsyncImage(
                                            model = uriString,
                                            contentDescription = "Фото в чате",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(180.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )

                                        if (textPart.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(text = textPart, color = Color.White, fontSize = 15.sp)
                                        }
                                    } else {
                                        if (message.text.isNotEmpty()) {
                                            Text(
                                                text = message.text,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                                fontSize = 15.sp,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ПРЕВЬЮ ВЫБРАННОГО ФОТО НАД ПОЛЕМ ВВОДА
            selectedImageUri?.let { uri ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1A1A1A))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = uri,
                        contentDescription = "Превью",
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Картинка готова к отправке", color = Color.Gray, fontSize = 14.sp)

                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { selectedImageUri = null }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Удалить", tint = Color.Red)
                    }
                }
            }

            // 2. НИЖНЯЯ ПАНЕЛЬ ВВОДА
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E1E1E))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // ИСПРАВЛЕНО: Кнопка "Плюс" вместо Скрепки для открытия галереи
                IconButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Прикрепить фото",
                        tint = Color(0xFFD7CCC8)
                    )
                }

                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Сообщение...", color = Color.Gray) },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 4.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF2B2B2B),
                        unfocusedContainerColor = Color(0xFF2B2B2B),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                // Кнопка Отправить
                Button(
                    onClick = {
                        if (inputText.isNotBlank() || selectedImageUri != null) {
                            val finalMessageText = if (selectedImageUri != null) {
                                "${inputText} 📸описание:${selectedImageUri.toString()}"
                            } else {
                                inputText
                            }

                            viewModel.sendMessage(chatId, finalMessageText)
                            inputText = ""
                            selectedImageUri = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6E63)),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text("Отпр.", color = Color.White, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Кнопка ТТК
                Button(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendMessage(chatId, inputText)
                            inputText = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E2723)),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text("ТТК", color = Color(0xFFD7CCC8), fontSize = 13.sp)
                }
            }
        }
    }
}