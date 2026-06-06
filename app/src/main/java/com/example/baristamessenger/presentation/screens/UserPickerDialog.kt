package com.example.baristamessenger.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.baristamessenger.domain.model.User
import com.example.baristamessenger.presentation.viewmodel.SearchUserViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun UserPickerDialog(
    onDismiss: () -> Unit,
    onUserSelected: (User) -> Unit,
    viewModel: SearchUserViewModel = koinViewModel()
) {

    val users by viewModel.users.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text("Выберите сотрудника")
        },

        text = {

            LazyColumn {

                items(users) { user ->

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onUserSelected(user)
                            }
                            .padding(12.dp)
                    ) {

                        Text("${user.firstName} ${user.lastName}".trim())

                        Text(user.role)
                    }
                }
            }
        },

        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть")
            }
        }
    )
}