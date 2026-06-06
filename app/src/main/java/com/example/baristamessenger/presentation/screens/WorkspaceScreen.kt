package com.example.baristamessenger.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Структура данных
data class InteractiveShift(
    val id: String = "",
    val date: String = "",
    val time: String = "",
    val location: String = "",
    val authorName: String = "",
    val authorId: String = "",  // ← добавить ID автора
    val comment: String = "",
    var status: ShiftStatus = ShiftStatus.OPENED,
    var baristaWhoResponded: String? = null,
    var baristaWhoRespondedId: String? = null  // ← добавить ID откликнувшегося
)

enum class ShiftStatus {
    OPENED,
    CONFIRMED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceScreen(
    onBackClick: () -> Unit,
    currentUserName: String = "Вы"
) {
    val uriHandler = LocalUriHandler.current
    val scheduleSheetUrl = "https://docs.google.com/spreadsheets/d/1s2oEnfiZcmVAkFspVlPsdlosdqBd4mKBzld01KgwOw0/edit?gid=0#gid=0"

    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // Получаем реальный никнейм пользователя из Firebase
    var realUserName by remember { mutableStateOf(currentUserName) }

    LaunchedEffect(Unit) {
        if (currentUserId.isNotEmpty()) {
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(currentUserId).get()
                .addOnSuccessListener { doc ->
                    val nickname = doc.getString("nickname")
                        ?: doc.getString("coffeeShop")
                        ?: doc.getString("email")?.substringBefore("@")
                        ?: currentUserName
                    realUserName = nickname
                }
        }
    }

    val isAdminMode = false
    val shiftList = remember { mutableStateListOf<InteractiveShift>() }

    // Состояния для редактирования
    var editingShift by remember { mutableStateOf<InteractiveShift?>(null) }
    var editDate by remember { mutableStateOf("") }
    var editTime by remember { mutableStateOf("") }
    var editLocation by remember { mutableStateOf("") }
    var editComment by remember { mutableStateOf("") }

    // Синхронизация с Firebase
    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("shifts").addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                shiftList.clear()
                for (doc in snapshot.documents) {
                    val statusString = doc.getString("status") ?: "OPENED"
                    val mappedStatus = when (statusString) {
                        "CONFIRMED" -> ShiftStatus.CONFIRMED
                        else -> ShiftStatus.OPENED
                    }

                    val shift = InteractiveShift(
                        id = doc.id,
                        date = doc.getString("date") ?: "",
                        time = doc.getString("time") ?: "",
                        location = doc.getString("location") ?: "",
                        authorName = doc.getString("authorName") ?: "",
                        authorId = doc.getString("authorId") ?: "",
                        comment = doc.getString("comment") ?: "",
                        status = mappedStatus,
                        baristaWhoResponded = doc.getString("baristaWhoResponded"),
                        baristaWhoRespondedId = doc.getString("baristaWhoRespondedId")
                    )
                    shiftList.add(shift)
                }
            }
        }
    }

    var showCreateDialog by remember { mutableStateOf(false) }
    var dateInput by remember { mutableStateOf("") }
    var timeInput by remember { mutableStateOf("") }
    var locationInput by remember { mutableStateOf("") }
    var commentInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Шифт-менеджмент",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Создать запрос",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E))
            )
        },
        containerColor = Color(0xFF121212)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // КНОПКА ГРАФИКА РАБОТЫ
                Button(
                    onClick = { uriHandler.openUri(scheduleSheetUrl) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "График",
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "ГРАФИК РАБОТЫ",
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (shiftList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Нет запросов.\nНажмите на плюс вверху, чтобы создать замену смены.",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(bottom = 16.dp, top = 0.dp)
                    ) {
                        items(shiftList) { shift ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C1B17))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    // Заголовок с кнопками
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Замена смены", color = Color(0xFFD7CCC8), fontWeight = FontWeight.Bold, fontSize = 18.sp)

                                        Row {
                                            // Редактирование только для открытых запросов автора
                                            if (shift.status == ShiftStatus.OPENED && shift.authorId == currentUserId) {
                                                IconButton(
                                                    onClick = {
                                                        editDate = shift.date
                                                        editTime = shift.time
                                                        editLocation = shift.location
                                                        editComment = shift.comment
                                                        editingShift = shift
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Edit,
                                                        contentDescription = "Редактировать",
                                                        tint = Color(0xFFFFD700),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }

                                            // Удаление для автора или админа
                                            if (shift.authorId == currentUserId || isAdminMode) {
                                                IconButton(
                                                    onClick = {
                                                        FirebaseFirestore.getInstance().collection("shifts")
                                                            .document(shift.id).delete()
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Удалить",
                                                        tint = Color(0xFFE57373),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    val statusText = if (shift.status == ShiftStatus.OPENED) "Открыта" else "Закреплена"
                                    val statusColor = if (shift.status == ShiftStatus.OPENED) Color(0xFFFFB74D) else Color(0xFF81C784)

                                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF3E2723)) {
                                        Text(statusText, color = statusColor, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 12.sp)
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Дата: ${shift.date}", color = Color.White, fontSize = 14.sp)
                                    Text("Время: ${shift.time}", color = Color.White, fontSize = 14.sp)
                                    Text("Точка: ${shift.location}", color = Color.White, fontSize = 14.sp)

                                    // === ИСПРАВЛЕНО: показываем инициатора ===
                                    Text(
                                        text = "Инициатор: ${if (shift.authorName.isNotEmpty()) shift.authorName else "Неизвестный"}",
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )

                                    if (shift.baristaWhoResponded != null) {
                                        Text(
                                            text = "Откликнулся: ${shift.baristaWhoResponded}",
                                            color = Color(0xFFFFF176),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    if (shift.comment.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(" ${shift.comment}", color = Color(0xFFE0E0E0), fontSize = 14.sp)
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    when {
                                        shift.status == ShiftStatus.CONFIRMED -> {
                                            Surface(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFF1B5E20)
                                            ) {
                                                Text(
                                                    text = "Уведомление: Смена успешно закреплена за сотрудником ${shift.baristaWhoResponded}!",
                                                    color = Color.White,
                                                    modifier = Modifier.padding(8.dp),
                                                    fontSize = 13.sp
                                                )
                                            }
                                        }

                                        shift.status == ShiftStatus.OPENED && shift.authorId != currentUserId -> {
                                            Button(
                                                onClick = {
                                                    val updatedShift = shift.copy(
                                                        status = ShiftStatus.CONFIRMED,
                                                        baristaWhoResponded = realUserName,
                                                        baristaWhoRespondedId = currentUserId
                                                    )
                                                    FirebaseFirestore.getInstance().collection("shifts")
                                                        .document(shift.id).set(updatedShift)
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D4037))
                                            ) {
                                                Text("Откликнуться на смену", color = Color.White)
                                            }
                                        }

                                        shift.status == ShiftStatus.OPENED && shift.authorId == currentUserId -> {
                                            Surface(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFF4A148C).copy(alpha = 0.5f)
                                            ) {
                                                Text(
                                                    text = "Это ваша смена. Ожидайте отклика другого сотрудника.",
                                                    color = Color.White.copy(alpha = 0.7f),
                                                    modifier = Modifier.padding(8.dp),
                                                    fontSize = 13.sp,
                                                    textAlign = TextAlign.Center
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
    }

    // Диалог создания новой смены
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = Color(0xFF1E1E1E),
            title = { Text("Новая замена", color = Color.White) },
            text = {
                Column {
                    OutlinedTextField(
                        value = dateInput,
                        onValueChange = { dateInput = it },
                        placeholder = { Text("Дата", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFFD700),
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(0xFFFFD700)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = timeInput,
                        onValueChange = { timeInput = it },
                        placeholder = { Text("Время", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFFD700),
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(0xFFFFD700)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = locationInput,
                        onValueChange = { locationInput = it },
                        placeholder = { Text("Адрес кофейни", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFFD700),
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(0xFFFFD700)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = commentInput,
                        onValueChange = { commentInput = it },
                        placeholder = { Text("Комментарий...", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFFD700),
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(0xFFFFD700)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (dateInput.isNotBlank() && timeInput.isNotBlank()) {
                            val db = FirebaseFirestore.getInstance()
                            val newDocRef = db.collection("shifts").document()

                            val newShift = InteractiveShift(
                                id = newDocRef.id,
                                date = dateInput,
                                time = timeInput,
                                location = locationInput.ifBlank { "Surf Coffee" },
                                authorName = realUserName,  // ← используем реальный никнейм
                                authorId = currentUserId,   // ← сохраняем ID автора
                                comment = commentInput,
                                status = ShiftStatus.OPENED
                            )

                            newDocRef.set(newShift)

                            dateInput = ""
                            timeInput = ""
                            locationInput = ""
                            commentInput = ""
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D4037))
                ) {
                    Text("Разместить", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Отмена", color = Color.Gray)
                }
            }
        )
    }

    // Диалог редактирования
    if (editingShift != null) {
        AlertDialog(
            onDismissRequest = { editingShift = null },
            containerColor = Color(0xFF1E1E1E),
            title = { Text("Редактировать запрос", color = Color.White) },
            text = {
                Column {
                    OutlinedTextField(
                        value = editDate,
                        onValueChange = { editDate = it },
                        placeholder = { Text("Дата", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFFD700),
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(0xFFFFD700)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editTime,
                        onValueChange = { editTime = it },
                        placeholder = { Text("Время", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFFD700),
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(0xFFFFD700)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editLocation,
                        onValueChange = { editLocation = it },
                        placeholder = { Text("Адрес кофейни", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFFD700),
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(0xFFFFD700)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editComment,
                        onValueChange = { editComment = it },
                        placeholder = { Text("Комментарий...", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFFD700),
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(0xFFFFD700)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editingShift != null && editDate.isNotBlank() && editTime.isNotBlank()) {
                            val updatedShift = editingShift!!.copy(
                                date = editDate,
                                time = editTime,
                                location = editLocation,
                                comment = editComment
                            )
                            FirebaseFirestore.getInstance().collection("shifts")
                                .document(editingShift!!.id)
                                .set(updatedShift)
                            editingShift = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                ) {
                    Text("Сохранить", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingShift = null }) {
                    Text("Отмена", color = Color.Gray)
                }
            }
        )
    }
}