package com.example.baristamessenger.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.baristamessenger.domain.model.Ingredient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CostCalculatorScreen() {
    var drinkName by remember { mutableStateOf("Лавандовый раф") }
    var drinkVolume by remember { mutableStateOf("350") }
    var markupPercentage by remember { mutableStateOf(300f) }

    val ingredients = remember {
        mutableStateListOf(
            Ingredient("1", "Эспрессо", 18, 6.50),
            Ingredient("2", "Молоко", 150, 9.00),
            Ingredient("3", "Апельсиновый сироп", 20, 4.00),
            Ingredient("4", "Сливки 33%", 30, 6.00)
        )
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var newIngName by remember { mutableStateOf("") }
    var newIngAmount by remember { mutableStateOf("") }
    var newIngPrice by remember { mutableStateOf("") }

    val totalCost = ingredients.sumOf { it.price }
    val recommendedPrice = totalCost * (1 + markupPercentage / 100)

    // Выносим общие стили для полей ввода, чтобы текст ВСЕГДА был белым
    val customTextFieldColors = TextFieldDefaults.colors(
        focusedTextColor = Color.White,       // ИСПРАВЛЕНО: Цвет текста при нажатии
        unfocusedTextColor = Color.White,     // ИСПРАВЛЕНО: Цвет текста в обычном состоянии
        focusedContainerColor = Color(0xFF1E1E1E),
        unfocusedContainerColor = Color(0xFF1E1E1E),
        focusedPlaceholderColor = Color.Gray,
        unfocusedPlaceholderColor = Color.Gray,
        focusedLabelColor = Color(0xFFD7CCC8),
        unfocusedLabelColor = Color.Gray
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Калькулятор себестоимости ☕", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E))
            )
        },
        containerColor = Color(0xFF121212)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Название напитка", color = Color.Gray, fontSize = 12.sp)
                TextField(
                    value = drinkName,
                    onValueChange = { drinkName = it },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = customTextFieldColors // Применяем белые цвета текста
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text("Объем напитка (мл)", color = Color.Gray, fontSize = 12.sp)
                TextField(
                    value = drinkVolume,
                    onValueChange = { drinkVolume = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = customTextFieldColors // Применяем белые цвета текста
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Ингредиенты", color = Color(0xFFD7CCC8), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(ingredients) { ingredient ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(ingredient.name, color = Color.White, fontWeight = FontWeight.Medium)
                            Text("${ingredient.amount} мл/г", color = Color.Gray, fontSize = 12.sp)
                        }
                        Text(String.format("%.2f ₽", ingredient.price), color = Color(0xFFD7CCC8), fontWeight = FontWeight.Bold)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD7CCC8))
                ) {
                    Text("+ Добавить ингредиент")
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2C1B17)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Себестоимость", color = Color.Gray)
                            Text(String.format("%.2f ₽", totalCost), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Наценка", color = Color.Gray)
                            Text("${markupPercentage.toInt()}%", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = markupPercentage,
                            onValueChange = { markupPercentage = it },
                            valueRange = 50f..500f,
                            colors = SliderDefaults.colors(thumbColor = Color(0xFFD7CCC8), activeTrackColor = Color(0xFF5D4037))
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.Gray.copy(alpha = 0.3f))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Рекомендуемая цена", color = Color.White, fontWeight = FontWeight.Bold)
                            Text(String.format("%.2f ₽", recommendedPrice), color = Color(0xFF81C784), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                    onClick = {
                        ingredients.clear()
                        markupPercentage = 300f
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Сбросить калькулятор", color = Color.Red.copy(alpha = 0.6f))
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = Color(0xFF1E1E1E),
            title = { Text("Новый ингредиент", color = Color.White) },
            text = {
                Column {
                    TextField(
                        value = newIngName,
                        onValueChange = { newIngName = it },
                        placeholder = { Text("Название (например: Молоко)") },
                        colors = customTextFieldColors // Применяем белые цвета текста
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = newIngAmount,
                        onValueChange = { newIngAmount = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = { Text("Количество (г/мл)") },
                        colors = customTextFieldColors // Применяем белые цвета текста
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = newIngPrice,
                        onValueChange = { newIngPrice = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = { Text("Стоимость порции (₽)") },
                        colors = customTextFieldColors // Применяем белые цвета текста
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = newIngAmount.toIntOrNull() ?: 0
                        val price = newIngPrice.toDoubleOrNull() ?: 0.0
                        if (newIngName.isNotBlank() && amount > 0 && price > 0.0) {
                            ingredients.add(Ingredient((ingredients.size + 1).toString(), newIngName, amount, price))
                            newIngName = ""
                            newIngAmount = ""
                            newIngPrice = ""
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D4037))
                ) {
                    Text("Добавить", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Отмена", color = Color.Gray)
                }
            }
        )
    }
}