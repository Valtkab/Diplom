package com.example.baristamessenger.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    onBackClick: () -> Unit
) {
    // Цвета интерфейса SOS-экрана из макета image_7be5c1.png
    val darkBackground = Color(0xFF121111)
    val sosRedCard = Color(0xFF4C1C1A)
    val buttonRed = Color(0xFF9E221E)
    val listCardBg = Color(0xFF1E1B1B)
    val secondaryText = Color(0xFF968F8F)
    val goldAccent = Color(0xFFC99B67)

    Scaffold(
        containerColor = darkBackground,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = darkBackground,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                title = { Text("Оборудование: помощь", fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { }) { Icon(Icons.Default.Search, contentDescription = "Поиск") }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Крупная карточка SOS заявки
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = sosRedCard),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Заголовок SOS
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFE55753))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("SOS! Нужна помощь", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                Text("Проблема с оборудованием", color = secondaryText, fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Поля заявки
                        SosField(label = "Оборудование", value = "La Marzocco Linea PB", labelColor = secondaryText)
                        SosField(label = "Проблема", value = "Потекла группа", labelColor = secondaryText)
                        SosField(label = "Точка", value = "Coffee Buzz, Тверская 12", labelColor = secondaryText)
                        SosField(label = "Комментарий", value = "Пошла вода из группы, пока не можем остановить.", labelColor = secondaryText)

                        Spacer(modifier = Modifier.height(16.dp))

                        // Кнопка отправки сигнала
                        Button(
                            onClick = { },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = buttonRed),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text("Отправить сигнал", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        }
                    }
                }
            }

            // Блок частых проблем
            item {
                Text(
                    text = "Частые проблемы",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            // Список элементов частых неисправностей
            val commonProblems = listOf(
                "Потекла группа" to "Что проверить и как решить",
                "Сбился помол" to "Решение и советы",
                "Слабый напор воды" to "Диагностика и решение"
            )

            items(commonProblems.size) { index ->
                val (title, sub) = commonProblems[index]
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = listCardBg),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.05f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Build, contentDescription = null, tint = goldAccent, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(sub, color = secondaryText, fontSize = 12.sp)
                        }
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = secondaryText)
                    }
                }
            }

            // Кнопка нижней базы знаний
            item {
                Button(
                    onClick = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF382C24)),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("☕", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("База знаний по оборудованию", color = Color(0xFFF1D3B3), fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SosField(label: String, value: String, labelColor: Color) {
    Column(modifier = Modifier.padding(bottom = 10.dp)) {
        Text(text = label, color = labelColor, fontSize = 12.sp)
        Text(text = value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 2.dp))
    }
}