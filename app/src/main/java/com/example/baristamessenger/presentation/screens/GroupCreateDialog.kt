package com.example.baristamessenger.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.baristamessenger.domain.model.User
import com.example.baristamessenger.presentation.viewmodel.SearchUserViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun GroupCreateDialog(
    onDismiss: () -> Unit,
    onCreate: (String, List<String>) -> Unit,
    viewModel: SearchUserViewModel = koinViewModel()
) {

    val users by viewModel.users.collectAsState()

    var groupName by remember { mutableStateOf("") }
    var selectedUsers by remember { mutableStateOf(setOf<String>()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E1E),
        title = {
            Text(
                text = "Создать группу",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                // Поле ввода названия группы с белым текстом
                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text("Название группы", color = Color.Gray) },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = Color.White,
                        fontSize = 16.sp
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
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Участники",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .height(250.dp)
                        .background(Color(0xFF2C2C2C), shape = RoundedCornerShape(12.dp))
                ) {
                    items(users) { user ->
                        val isSelected = selectedUsers.contains(user.id)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedUsers = if (isSelected) {
                                        selectedUsers - user.id
                                    } else {
                                        selectedUsers + user.id
                                    }
                                }
                                .background(
                                    if (isSelected) Color(0xFFFFD700).copy(alpha = 0.3f)
                                    else Color.Transparent
                                )
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Квадратик для отметки
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .border(
                                        width = 1.5.dp,
                                        color = if (isSelected) Color(0xFFFFD700) else Color.Gray,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .background(
                                        color = if (isSelected) Color(0xFFFFD700) else Color.Transparent,
                                        shape = RoundedCornerShape(4.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Text(
                                        text = "✓",
                                        color = Color.Black,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = "${user.firstName} ${user.lastName}".trim(),
                                color = if (isSelected) Color(0xFFFFD700) else Color.White,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (groupName.isNotBlank() && selectedUsers.isNotEmpty()) {
                        onCreate(groupName, selectedUsers.toList())
                    }
                },
                enabled = groupName.isNotBlank() && selectedUsers.isNotEmpty()
            ) {
                Text(
                    text = "Создать",
                    color = if (groupName.isNotBlank() && selectedUsers.isNotEmpty())
                        Color(0xFFFFD700)
                    else
                        Color.Gray,
                    fontSize = 14.sp
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Отмена",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }
    )
}