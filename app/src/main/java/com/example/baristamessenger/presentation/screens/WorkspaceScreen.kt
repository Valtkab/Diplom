package com.example.baristamessenger.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons // ДОБАВЛЕН ИМПОРТ
import androidx.compose.material.icons.automirrored.filled.ArrowBack // ДОБАВЛЕН ИМПОРТ
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Модель данных остается прежней
data class InteractiveShift(
    val id: String,
    val date: String,
    val time: String,
    val location: String,
    val authorName: String,
    val comment: String,
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
    onBackClick: () -> Unit // Параметр успешно подключен
) {

    // 💡 ТЕПЕРЬ ТУТ НЕТ ТУМБЛЛЕРА!
    // Изменяй это значение вручную для тестирования:
    // false — интерфейс для Бариста, true — интерфейс для Администратора.
    val isAdminMode = false

    // Имитация базы данных смен
    val shiftList = remember {
        mutableStateListOf(
            InteractiveShift("1", "Завтра, 27 мая", "08:00 - 16:00", "Surf Coffee x Arbat", "Анна", "Не получается выйти, нужен герой на утро 🙏", ShiftStatus.OPENED),
            InteractiveShift("2", "29 мая, пт", "12:00 - 20:00", "Даблби на Патриках", "Максим", "Ребята, выручайте, нужно на приём к врачу.", ShiftStatus.AWAITING_CONFIRMATION, "Иван Соколов")
        )
    }

    var showCreateDialog by remember { mutableStateOf(false) }
    var dateInput by remember { mutableStateOf("") }
    var timeInput by remember { mutableStateOf("") }
    var locationInput by remember { mutableStateOf("") }
    var commentInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Шифт-менеджмент", color = Color.White) },
                // ИСПРАВЛЕНО: Добавлена кнопка "Назад"
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 80.dp, top = 8.dp)
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

                            // Бизнес-логика кнопок работает автоматически в зависимости от isAdminMode
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
                                                shiftList[index] = shift.copy(status = ShiftStatus.CONFIRMED)
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
                                                shiftList[index] = shift.copy(
                                                    status = ShiftStatus.AWAITING_CONFIRMATION,
                                                    baristaWhoResponded = "Иван Соколов"
                                                )
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

            // Кнопка создания новой смены
            Button(
                onClick = { showCreateDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD7CCC8))
            ) {
                Text("+ Создать запрос", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }

    // Диалог создания
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
                            shiftList.add(
                                InteractiveShift(
                                    id = (shiftList.size + 1).toString(),
                                    date = dateInput,
                                    time = timeInput,
                                    location = locationInput.ifBlank { "Surf Coffee" },
                                    authorName = if (isAdminMode) "Управляющий" else "Бариста",
                                    comment = commentInput,
                                    status = ShiftStatus.OPENED
                                )
                            )
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