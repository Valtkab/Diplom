package com.example.baristamessenger.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
            Text("Создать группу", color = Color.White)
        },
        text = {

            Column {

                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text("Название группы") }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Участники", color = Color.White)

                LazyColumn(
                    modifier = Modifier.height(250.dp)
                ) {

                    items(users) { user ->

                        val isSelected = selectedUsers.contains(user.id)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedUsers =
                                        if (isSelected) {
                                            selectedUsers - user.id
                                        } else {
                                            selectedUsers + user.id
                                        }
                                }
                                .padding(8.dp)
                                .background(
                                    if (isSelected) Color.White
                                    else Color.Transparent
                                )
                        ) {

                            Text(
                                text = user.name,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (groupName.isNotBlank()) {
                        onCreate(groupName, selectedUsers.toList())
                    }
                }
            ) {
                Text("Создать", color = Color(0xFFFFD700))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = Color.Gray)
            }
        }
    )
}