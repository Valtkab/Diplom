package com.example.baristamessenger.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.baristamessenger.presentation.viewmodel.OrderViewModel
import org.koin.androidx.compose.koinViewModel

data class OrderItem(
    val id: Long,
    val table: String,
    val description: String // Теперь это просто строка с составом заказа
)

@Composable
fun OrderScreen(viewModel: OrderViewModel = koinViewModel()) {
    val orderItems by viewModel.orderItems.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFF121212),
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }, containerColor = Color(0xFFFFD700)) {
                Icon(Icons.Default.Add, contentDescription = "Добавить", tint = Color.Black)
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Text("Текущие заказы", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold) }
            items(orderItems) { item ->
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Стол: ${item.table}", color = Color.Gray, fontSize = 12.sp)
                            // Состав заказа белым цветом
                            Text(text = item.description, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        }
                        IconButton(onClick = { viewModel.removeOrder(item.id) }, colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFF2E7D32))) {
                            Icon(Icons.Default.Check, contentDescription = "Выполнено", tint = Color.White)
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        OrderAddDialog(
            onDismiss = { showDialog = false },
            onConfirm = { table, desc ->
                viewModel.addOrder(OrderItem(System.currentTimeMillis(), table, desc))
                showDialog = false
            }
        )
    }
}

@Composable
fun OrderAddDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var table by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E1E),
        title = { Text("Новый заказ", color = Color.White) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = table, onValueChange = { table = it }, label = { Text("Номер стола") }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                Spacer(modifier = Modifier.height(8.dp))
                // Свободный ввод состава заказа
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Что в заказе?") },
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(table, description) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))) {
                Text("Добавить", color = Color.Black)
            }
        }
    )
}