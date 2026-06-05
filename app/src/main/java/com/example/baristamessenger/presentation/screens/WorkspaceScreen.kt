package com.example.baristamessenger.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
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
import com.google.firebase.firestore.FirebaseFirestore // 👈 ДОБАВЛЕН ИМПОРТ ДЛЯ БАЗЫ ДАННЫХ

// Структура данных
data class InteractiveShift(
    val id: String = "",                     // 👈 Добавлены дефолтные значения для Firebase
    val date: String = "",                   // 👈 Добавлены дефолтные значения для Firebase
    val time: String = "",                   // 👈 Добавлены дефолтные значения для Firebase
    val location: String = "",               // 👈 Добавлены дефолтные значения для Firebase
    val authorName: String = "",             // 👈 Добавлены дефолтные значения для Firebase
    val comment: String = "",                // 👈 Добавлены дефолтные значения для Firebase
    var status: ShiftStatus = ShiftStatus.OPENED,
    var baristaWhoResponded: String? = null
)

enum class ShiftStatus {
    OPENED,
    AWAITING_CONFIRMATION,
    CONFIRMED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceScreen(
    onBackClick: () -> Unit,
    currentUserName: String = "Вы" // 👈 Дефолтное значение спасает от ошибок в других файлах!
) {
    val uriHandler = LocalUriHandler.current
    val scheduleSheetUrl = "https://docs.google.com/spreadsheets/d/1s2oEnfiZcmVAkFspVlPsdlosdqBd4mKBzld01KgwOw0/edit?gid=0#gid=0"

    // false — интерфейс для Бариста, true — для Администратора
    val isAdminMode = false

    // Список абсолютно пустой на старте
    val shiftList = remember { mutableStateListOf<InteractiveShift>() }

    // 🔴 ДОБАВЛЕНО: Синхронизация с Firebase Firestore в реальном времени
    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance().collection("shifts")
            .addSnapshotListener { snapshot, error ->
                if (snapshot != null) {
                    shiftList.clear()
                    val remoteShifts = snapshot.toObjects(InteractiveShift::class.java)
                    shiftList.addAll(remoteShifts)
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

                // Отображение контента
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
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Замена смены", color = Color(0xFFD7CCC8), fontWeight = FontWeight.Bold, fontSize = 18.sp)

                                        val (statusText, statusColor) = when (shift.status) {
                                            ShiftStatus.OPENED -> "Открыта" to Color(0xFFFFB74D)
                                            ShiftStatus.AWAITING_CONFIRMATION -> "Ожидает админа" to Color(0xFF64B5F6)
                                            ShiftStatus.CONFIRMED -> "Подтверждена" to Color(0xFF81C784)
                                        }

                                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF3E2723)) {
                                            Text(statusText, color = statusColor, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 12.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Дата: ${shift.date}", color = Color.White, fontSize = 14.sp)
                                    Text("Время: ${shift.time}", color = Color.White, fontSize = 14.sp)
                                    Text("Точка: ${shift.location}", color = Color.White, fontSize = 14.sp)
                                    Text("Инициатор: ${shift.authorName}", color = Color.Gray, fontSize = 14.sp)

                                    if (shift.baristaWhoResponded != null) {
                                        Text("Откликнулся: ${shift.baristaWhoResponded}", color = Color(0xFFFFF176), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(shift.comment, color = Color(0xFFE0E0E0), fontSize = 14.sp)

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

                                        isAdminMode && shift.status == ShiftStatus.AWAITING_CONFIRMATION -> {
                                            Button(
                                                onClick = {
                                                    val index = shiftList.indexOf(shift)
                                                    if (index != -1) {
                                                        val updatedShift = shift.copy(status = ShiftStatus.CONFIRMED)
                                                        shiftList[index] = updatedShift

                                                        // 👈 ДОБАВЛЕНО: Обновление статуса в Firebase
                                                        FirebaseFirestore.getInstance().collection("shifts")
                                                            .document(shift.id).set(updatedShift)
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
                                            ) {
                                                Text("Принять отклик (Подтвердить)", color = Color.White)
                                            }
                                        }

                                        !isAdminMode && shift.status == ShiftStatus.OPENED -> {
                                            Button(
                                                onClick = {
                                                    val index = shiftList.indexOf(shift)
                                                    if (index != -1) {
                                                        val updatedShift = shift.copy(
                                                            status = ShiftStatus.AWAITING_CONFIRMATION,
                                                            baristaWhoResponded = currentUserName // 👈 Вместо Ивана Соколова подставляется никнейм
                                                        )
                                                        shiftList[index] = updatedShift

                                                        // 👈 ДОБАВЛЕНО: Обновление отклика в Firebase
                                                        FirebaseFirestore.getInstance().collection("shifts")
                                                            .document(shift.id).set(updatedShift)
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D4037))
                                            ) {
                                                Text("Откликнуться на смену", color = Color.White)
                                            }
                                        }

                                        shift.status == ShiftStatus.AWAITING_CONFIRMATION -> {
                                            Text("Ожидайте подтверждения управляющего кофейни", color = Color.Gray, fontSize = 13.sp)
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

    // Диалог создания новой смены пользователем
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = Color(0xFF1E1E1E),
            title = { Text("Новая замена", color = Color.White) },
            text = {
                Column {
                    TextField(value = dateInput, onValueChange = { dateInput = it }, placeholder = { Text("Дата") })
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(value = timeInput, onValueChange = { timeInput = it }, placeholder = { Text("Время") })
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(value = locationInput, onValueChange = { locationInput = it }, placeholder = { Text("Адрес кофейни") })
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(value = commentInput, onValueChange = { commentInput = it }, placeholder = { Text("Комментарий...") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (dateInput.isNotBlank() && timeInput.isNotBlank()) {
                            // 👈 ДОБАВЛЕНО: Создаем ссылку на новый документ для получения уникального ID из Firebase
                            val db = FirebaseFirestore.getInstance()
                            val newDocRef = db.collection("shifts").document()

                            val newShift = InteractiveShift(
                                id = newDocRef.id, // 👈 Передаем уникальный ID документа из Firebase
                                date = dateInput,
                                time = timeInput,
                                location = locationInput.ifBlank { "Surf Coffee" },
                                authorName = if (isAdminMode) "Управляющий" else currentUserName, // 👈 Инициатором теперь тоже пишется никнейм создателя
                                comment = commentInput,
                                status = ShiftStatus.OPENED
                            )

                            shiftList.add(newShift)

                            // 👈 ДОБАВЛЕНО: Отправляем объект смены в Firebase
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
}