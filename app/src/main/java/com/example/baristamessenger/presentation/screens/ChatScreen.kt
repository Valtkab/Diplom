package com.example.baristamessenger.presentation.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.baristamessenger.presentation.viewmodel.ChatViewModel
import com.google.firebase.auth.FirebaseAuth
import org.koin.androidx.compose.koinViewModel
import java.io.File
import java.io.FileOutputStream
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.firestore.FirebaseFirestore


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
        viewModel.migrateOldMessages()
    }

    val messages by viewModel.messages.collectAsState()
    var inputText by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var enlargedImageUri by remember { mutableStateOf<String?>(null) }
    var showChooserDialog by remember { mutableStateOf(false) }
    var editingMessage by remember { mutableStateOf<com.example.baristamessenger.domain.model.Message?>(null) }
    var editText by remember { mutableStateOf("") }

    // Определяем, групповой ли чат
    var isGroupChat by remember { mutableStateOf(false) }

    LaunchedEffect(chatId) {
        val db = FirebaseFirestore.getInstance()
        db.collection("chats").document(chatId).get()
            .addOnSuccessListener { doc ->
                val participants = doc.get("participants") as? List<*> ?: emptyList<Any>()
                // Если участников больше 2 — это групповой чат
                isGroupChat = participants.size > 2 || doc.getBoolean("isGroup") == true
            }
    }

    var selectedMessageForAction by remember {
        mutableStateOf<com.example.baristamessenger.domain.model.Message?>(null)
    }

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
                title = {
                    Text(
                        "Чат кофейни",
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = Color(0xFFFFD700)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black
                )
            )
        },
        containerColor = Color(0xFF121212),
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .navigationBarsPadding()
                .imePadding()
        ) {

            val scrollState = rememberLazyListState()

            LaunchedEffect(messages.size) {
                if (messages.isNotEmpty()) {
                    scrollState.animateScrollToItem(0)
                }
            }

            LazyColumn(
                state = scrollState,
                reverseLayout = true,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Bottom),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(
                    items = messages.sortedByDescending { it.timestamp },
                    key = { it.id }
                ) { message ->

                    val isCurrentUser = message.senderId == currentUserId

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement =
                            if (isCurrentUser) Arrangement.End
                            else Arrangement.Start
                    ) {
                        Column(
                            modifier = Modifier.widthIn(max = 280.dp),
                            horizontalAlignment =
                                if (isCurrentUser) Alignment.End
                                else Alignment.Start
                        ) {
                            // === ИСПРАВЛЕНО: показываем никнейм для групповых чатов и чужих сообщений ===
                            if (isGroupChat && !isCurrentUser) {
                                Text(
                                    text = message.senderNickname.ifEmpty { "Бариста" },
                                    fontSize = 12.sp,
                                    color = Color(0xFFFFD700),  // Золотой цвет для никнеймов
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(
                                        start = 4.dp,
                                        bottom = 2.dp
                                    )
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(
                                    topStart = 12.dp,
                                    topEnd = 12.dp,
                                    bottomStart = if (isCurrentUser) 12.dp else 2.dp,
                                    bottomEnd = if (isCurrentUser) 2.dp else 12.dp
                                ),
                                color = if (isCurrentUser)
                                    Color(0xFFFFD700)
                                else
                                    Color(0xFF333333),
                                modifier = Modifier.combinedClickable(
                                    onClick = {},
                                    onLongClick = {
                                        selectedMessageForAction = message
                                    }
                                )
                            ){
                                Column(modifier = Modifier.padding(8.dp)) {
                                    if (message.text.contains("описание:")) {
                                        val textPart = message.text.substringBefore("описание:").trim()
                                        val uriString = message.text.substringAfter("описание:").trim()

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
                                                color = if (isCurrentUser) Color.Black else Color.White,
                                                fontSize = 15.sp,
                                                modifier = Modifier.padding(horizontal = 4.dp)
                                            )
                                        }
                                    } else {
                                        if (message.text.isNotEmpty()) {
                                            Text(
                                                text = message.text,
                                                modifier = Modifier.padding(
                                                    horizontal = 6.dp,
                                                    vertical = 2.dp
                                                ),
                                                fontSize = 15.sp,
                                                color = if (isCurrentUser) Color.Black else Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ПРЕВЬЮ ПРИКРЕПЛЕННОГО ФОТО
            selectedImageUri?.let { uri ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF2C2C2C))
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
                    Text("Изображение готово", color = Color.White, fontSize = 14.sp)

                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { selectedImageUri = null }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Удалить",
                            tint = Color(0xFFFFD700)
                        )
                    }
                }
            }

            // НИЖНЯЯ ПАНЕЛЬ ВВОДА
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E1E1E))
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { showChooserDialog = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Прикрепить",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 45.dp, max = 120.dp)
                        .background(Color(0xFF333333), shape = RoundedCornerShape(20.dp))
                        .border(1.dp, Color(0xFF555555), shape = RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (inputText.isEmpty()) {
                        Text("Сообщение...", color = Color.Gray, fontSize = 15.sp)
                    }
                    BasicTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        textStyle = TextStyle(color = Color.White, fontSize = 15.sp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                val isSendEnabled = inputText.isNotBlank() || selectedImageUri != null

                Button(
                    onClick = {
                        val imageUri = selectedImageUri

                        if (imageUri != null) {
                            MediaManager.get()
                                .upload(imageUri)
                                .callback(object : UploadCallback {
                                    override fun onStart(requestId: String?) {}
                                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                                    override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                                        val imageUrl = resultData?.get("secure_url").toString()
                                        val messageText = "$inputText описание:$imageUrl"
                                        viewModel.sendMessage(chatId, messageText)
                                        inputText = ""
                                        selectedImageUri = null
                                    }
                                    override fun onError(requestId: String?, error: ErrorInfo?) {
                                        error?.description?.let { println(it) }
                                    }
                                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                                })
                                .dispatch()
                        } else {
                            viewModel.sendMessage(chatId, inputText)
                            inputText = ""
                        }
                    },
                    enabled = isSendEnabled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD700),
                        disabledContainerColor = Color(0xFFFFD700).copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(45.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Отправить",
                        tint = if (isSendEnabled) Color.Black else Color.Gray,
                        modifier = Modifier.graphicsLayer { rotationZ = 180f }
                    )
                }
            }
        }

        // ДИАЛОГ ВЫБОРА ДЕЙСТВИЯ (РЕДАКТИРОВАТЬ / УДАЛИТЬ)
        if (selectedMessageForAction != null) {
            AlertDialog(
                onDismissRequest = { selectedMessageForAction = null },
                containerColor = Color(0xFF333333),
                title = {
                    Text(
                        "Действие с сообщением",
                        fontSize = 18.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Кнопка "Редактировать"
                        Button(
                            onClick = {
                                val msg = selectedMessageForAction
                                if (msg != null) {
                                    // Для фото-сообщений берём только текстовую часть
                                    if (msg.text.contains("описание:")) {
                                        editText = msg.text.substringBefore("описание:").trim()
                                    } else {
                                        editText = msg.text
                                    }
                                    editingMessage = msg
                                    selectedMessageForAction = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Редактировать", color = Color.Black, fontWeight = FontWeight.Medium)
                        }

                        // Кнопка "Удалить"
                        Button(
                            onClick = {
                                val msg = selectedMessageForAction
                                if (msg != null && msg.id.isNotEmpty()) {
                                    viewModel.deleteMessageWithImage(chatId, msg)
                                }
                                selectedMessageForAction = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Удалить сообщение", color = Color.White)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { selectedMessageForAction = null }) {
                        Text("Отмена", color = Color(0xFFFFD700), fontSize = 14.sp)
                    }
                }
            )
        }

// ДИАЛОГ РЕДАКТИРОВАНИЯ СООБЩЕНИЯ
        if (editingMessage != null) {
            var tempEditText by remember { mutableStateOf(editText) }

            LaunchedEffect(editText) {
                tempEditText = editText
            }

            AlertDialog(
                onDismissRequest = {
                    editingMessage = null
                    editText = ""
                },
                containerColor = Color(0xFF1E1E1E),
                title = {
                    Text(
                        "Редактировать сообщение",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        OutlinedTextField(
                            value = tempEditText,
                            onValueChange = { tempEditText = it },
                            label = { Text("Новый текст", color = Color.Gray) },
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 15.sp
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFFD700),
                                unfocusedBorderColor = Color.Gray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = Color(0xFFFFD700),
                                unfocusedLabelColor = Color.Gray,
                                cursorColor = Color(0xFFFFD700)
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 5
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Было: ${editingMessage?.text?.take(50)}",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (tempEditText.isNotBlank() && editingMessage != null) {
                                val originalMessage = editingMessage!!
                                var newText = tempEditText

                                if (originalMessage.text.contains("описание:")) {
                                    val uriString = originalMessage.text.substringAfter("описание:")
                                    newText = "$tempEditText описание:$uriString"
                                }

                                viewModel.editMessage(
                                    chatId = chatId,
                                    messageId = originalMessage.id,
                                    newText = newText
                                )
                                editingMessage = null
                                editText = ""
                            }
                        },
                        enabled = tempEditText.isNotBlank()
                    ) {
                        Text(
                            "Сохранить",
                            color = if (tempEditText.isNotBlank()) Color(0xFFFFD700) else Color.Gray,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        editingMessage = null
                        editText = ""
                    }) {
                        Text("Отмена", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            )
        }

        // ДИАЛОГ ВЫБОРА ИСТОЧНИКА ФОТО
        if (showChooserDialog) {
            AlertDialog(
                onDismissRequest = { showChooserDialog = false },
                title = { Text("Добавить изображение", color = Color.White, fontSize = 18.sp) },
                containerColor = Color(0xFF333333),
                text = {
                    Column {
                        Button(
                            onClick = {
                                showChooserDialog = false
                                galleryLauncher.launch("image/*")
                            },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                        ) {
                            Text("Открыть галерею", color = Color.Black)
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
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D4037))
                        ) {
                            Text("Сделать снимок (Камера)", color = Color.White)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showChooserDialog = false }) {
                        Text("Отмена", color = Color(0xFFFFD700))
                    }
                }
            )
        }

        // ПОЛНОЭКРАННЫЙ ПРОСМОТР ФОТО (ЗУМ)
        if (enlargedImageUri != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.95f))
                    .clickable { enlargedImageUri = null }
            ) {
                var scale by remember { mutableStateOf(1f) }
                var offsetX by remember { mutableStateOf(0f) }
                var offsetY by remember { mutableStateOf(0f) }

                val transformState = rememberTransformableState { zoomChange, panChange, _ ->
                    scale = (scale * zoomChange).coerceIn(1f, 4f)
                    offsetX += panChange.x
                    offsetY += panChange.y
                }

                LaunchedEffect(scale) {
                    if (scale <= 1.1f) {
                        offsetX = 0f
                        offsetY = 0f
                    }
                }

                AsyncImage(
                    model = enlargedImageUri,
                    contentDescription = "Просмотр фото",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 800.dp)
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offsetX,
                            translationY = offsetY
                        )
                        .transformable(state = transformState)
                        .align(Alignment.Center),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}