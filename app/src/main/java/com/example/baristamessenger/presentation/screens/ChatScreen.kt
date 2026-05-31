package com.example.baristamessenger.presentation.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi // ДОБАВЛЕНО
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable // ДОБАВЛЕНО
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.baristamessenger.presentation.viewmodel.ChatViewModel
import com.google.firebase.auth.FirebaseAuth
import org.koin.androidx.compose.koinViewModel
import java.io.File
import java.io.FileOutputStream

// Функция копирования фото во внутреннюю память
fun copyUriToInternalStorage(context: Context, uri: Uri): Uri {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val fileName = "chat_img_${System.currentTimeMillis()}.jpg"
        val outputFile = File(context.filesDir, fileName)

        inputStream?.use { input ->
            FileOutputStream(outputFile).use { output ->
                input.copyTo(output)
            }
        }
        Uri.fromFile(outputFile)
    } catch (e: Exception) {
        e.printStackTrace()
        uri
    }
}

// Вспомогательная функция для работы с камерой
fun createImageFileUri(context: Context): Uri {
    val imageFolder = File(context.cacheDir, "camera_images")
    if (!imageFolder.exists()) {
        imageFolder.mkdirs()
    }
    val tempFile = File(imageFolder, "camera_photo_${System.currentTimeMillis()}.jpg")

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        tempFile
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatScreen(
    chatId: String,
    onBackClick: () -> Unit,
    viewModel: ChatViewModel = koinViewModel()
) {
    val context = LocalContext.current
    LaunchedEffect(chatId) {
        viewModel.loadMessages(chatId)
    }

    val messages by viewModel.messages.collectAsState()
    var inputText by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }


    var enlargedImageUri by remember { mutableStateOf<String?>(null) }
    var showChooserDialog by remember { mutableStateOf(false) }

    // НОВОЕ: Переменная для хранения сообщения, на которое кликнули долго
    // Предполагается что тип сообщения - com.example.baristamessenger.domain.model.Message
    // Я использую Any? чтобы не было ошибок компиляции, замени Any на свой класс Message!
    // В ChatScreen.kt, в начале функции
    var selectedMessageForAction by remember { mutableStateOf<com.example.baristamessenger.domain.model.Message?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) selectedImageUri = uri
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            selectedImageUri = tempCameraUri
        }
    }

    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Чат кофейни ☕", color = Color.Black, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFE8E2FA))
            )
        },
        containerColor = Color.White
    ) { paddingValues ->

        Box(modifier = Modifier.fillMaxSize()) {
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
                    messages.reversed().let { reversedList ->
                        items(reversedList) { message ->
                            // ВНИМАНИЕ: Замени message.senderId на свои реальные свойства
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
                                            color = Color.Gray,
                                            modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                                        )
                                    }

                                    // НОВОЕ: Обрабатываем долгое нажатие на сообщение (удаление / реакции)
                                    Surface(
                                        shape = RoundedCornerShape(
                                            topStart = 12.dp,
                                            topEnd = 12.dp,
                                            bottomStart = if (isCurrentUser) 12.dp else 0.dp,
                                            bottomEnd = if (isCurrentUser) 0.dp else 12.dp
                                        ),
                                        color = if (isCurrentUser) Color(0xFF6750A4) else Color(0xFFF2F0F7),
                                        modifier = Modifier
                                            .padding(vertical = 2.dp)
                                            .combinedClickable(
                                                onClick = {},
                                                onLongClick = { selectedMessageForAction = message } // Открываем меню
                                            )
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {

                                            if (message.text.contains("📸описание:")) {
                                                val textPart = message.text.substringBefore("📸описание:").trim()
                                                val uriString = message.text.substringAfter("📸описание:").trim()

                                                AsyncImage(
                                                    model = uriString,
                                                    contentDescription = "Фото в чате",
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .wrapContentHeight()
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .clickable { enlargedImageUri = uriString },
                                                    contentScale = ContentScale.Fit
                                                )

                                                if (textPart.isNotEmpty()) {
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Text(
                                                        text = textPart,
                                                        color = if (isCurrentUser) Color.White else Color.Black,
                                                        fontSize = 15.sp,
                                                        modifier = Modifier.padding(horizontal = 4.dp)
                                                    )
                                                }
                                            } else {
                                                if (message.text.isNotEmpty()) {
                                                    Text(
                                                        text = message.text,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                                        fontSize = 15.sp,
                                                        color = if (isCurrentUser) Color.White else Color.Black
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // НОВОЕ: Отображение реакций под сообщением
                                    // (Для этого добавь val reactions: Map<String, Int> = emptyMap() в свою модель Message)
                                    // Закомментировано, чтобы не ломать сборку до обновления модели

                                    if (message.reactions.isNotEmpty()) {
                                        Row(modifier = Modifier.padding(top = 2.dp)) {
                                            message.reactions.forEach { (emoji, count) ->
                                                if (count > 0) {
                                                    Surface(
                                                        shape = RoundedCornerShape(12.dp),
                                                        color = Color(0xFFE5E0F4),
                                                        modifier = Modifier.padding(end = 4.dp)
                                                    ) {
                                                        Text(
                                                            text = "$emoji $count",
                                                            fontSize = 12.sp,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                }
                            }
                        }
                    }
                }

                // ПРЕВЬЮ КАРТИНКИ
                selectedImageUri?.let { uri ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF2F0F7))
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
                        Text("Изображение готово", color = Color.Black, fontSize = 14.sp)

                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = { selectedImageUri = null }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Удалить", tint = Color.Red)
                        }
                    }
                }

                // 2. НИЖНЯЯ ПАНЕЛЬ
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF3EDFA))
                        .padding(horizontal = 12.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { showChooserDialog = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Прикрепить", tint = Color.Black)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(45.dp)
                            .background(Color(0xFFE5E0F4), shape = RoundedCornerShape(4.dp))
                            .border(1.dp, Color(0xFFC7C2D9), shape = RoundedCornerShape(4.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (inputText.isEmpty()) {
                            Text("Сообщение...", color = Color.Gray, fontSize = 15.sp)
                        }
                        BasicTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            textStyle = TextStyle(color = Color.Black, fontSize = 15.sp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            if (inputText.isNotBlank() || selectedImageUri != null) {
                                // 💡 ЧТОБЫ ФОТО ВИДЕЛИ ДРУГИЕ:
                                // Здесь нужно не просто копировать Uri локально, а загружать его в Firebase Storage!
                                // val downloadUrl = viewModel.uploadImageToStorage(selectedImageUri)
                                // И затем использовать этот downloadUrl в строке ниже вместо finalUri

                                val finalUri = selectedImageUri?.let { copyUriToInternalStorage(context, it) }
                                val finalMessageText = if (finalUri != null) {
                                    "${inputText} 📸описание:${finalUri.toString()}"
                                } else {
                                    inputText
                                }
                                viewModel.sendMessage(chatId, finalMessageText)
                                inputText = ""
                                selectedImageUri = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text("Отпр.", color = Color.White, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Button(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                viewModel.sendMessage(chatId, inputText)
                                inputText = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7A5765)),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text("ТТК", color = Color.White, fontSize = 14.sp)
                    }
                }
            }

            // НОВОЕ: Диалоговое окно по долгому клику на сообщение (Реакции и Удаление)
            if (selectedMessageForAction != null) {
                // Каст к твоему классу (поменяй Any на твой Message класс!)
                // val message = selectedMessageForAction as Message

                AlertDialog(
                    onDismissRequest = { selectedMessageForAction = null },
                    containerColor = Color.White,
                    title = { Text("Действия с сообщением", fontSize = 18.sp) },
                    text = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("Реакции:", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                listOf("👍", "❤️", "🔥", "☕", "😂").forEach { emoji ->
                                    Text(
                                        text = emoji,
                                        fontSize = 24.sp,
                                        modifier = Modifier
                                            .clickable {
                                                // ПРИМЕЧАНИЕ: Вызови метод из ViewModel для добавления реакции
                                                // viewModel.addReaction(message.id, emoji)
                                                selectedMessageForAction = null
                                            }
                                            .padding(8.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Показываем кнопку удаления только если это наше сообщение
                            // if (message.senderId == currentUserId) {
                            Button(
                                onClick = {
                                    // Проверяем, что объект не пуст, и обращаемся к его полю .id
                                    val msg = selectedMessageForAction
                                    if (msg != null && msg.id.isNotEmpty()) {
                                        // Вызываем метод ViewModel, передавая chatId и ID конкретного сообщения
                                        viewModel.deleteMessage(chatId, msg.id)
                                    }
                                    selectedMessageForAction = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Удалить сообщение", color = Color.White)
                            }
                            // }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { selectedMessageForAction = null }) {
                            Text("Отмена", color = Color.Gray)
                        }
                    }
                )
            }

            // ДИАЛОГ ВЫБОРА ФОТО
            if (showChooserDialog) {
                AlertDialog(
                    onDismissRequest = { showChooserDialog = false },
                    title = { Text("Добавить изображение", color = Color.Black, fontSize = 18.sp) },
                    containerColor = Color.White,
                    text = {
                        Column {
                            Button(
                                onClick = {
                                    showChooserDialog = false
                                    galleryLauncher.launch("image/*")
                                },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
                            ) {
                                Text("Открыть галерею", color = Color.White)
                            }
                            Button(
                                onClick = {
                                    showChooserDialog = false
                                    try {
                                        val uri = createImageFileUri(context)
                                        tempCameraUri = uri
                                        cameraLauncher.launch(uri)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7A5765))
                            ) {
                                Text("Сделать снимок (Камера)", color = Color.White)
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showChooserDialog = false }) {
                            Text("Отмена", color = Color.Black)
                        }
                    }
                )
            }
        }
    }

    // ОКНО ПРОСМОТРА НА ВЕСЬ ЭКРАН
    enlargedImageUri?.let { uri ->
        Dialog(onDismissRequest = { enlargedImageUri = null }) {
            var scale by remember { mutableStateOf(1f) }
            var offset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }

            val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
                scale = (scale * zoomChange).coerceIn(1f, 4f)
                offset += offsetChange
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.95f))
                    .clickable { enlargedImageUri = null },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    AsyncImage(
                        model = uri,
                        contentDescription = "Фото во весь экран с зумом",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 550.dp)
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offset.x,
                                translationY = offset.y
                            )
                            .transformable(state = transformState),
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (scale == 1f) {
                        Button(
                            onClick = { enlargedImageUri = null },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
                        ) {
                            Text("Закрыть", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}