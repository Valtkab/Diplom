package com.example.baristamessenger.presentation.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.baristamessenger.presentation.viewmodel.SearchUserViewModel

@Composable
fun SearchUserScreen(
    viewModel: SearchUserViewModel = viewModel()
) {

    var searchText by remember {
        mutableStateOf("")
    }

    val users by viewModel.users.collectAsState()

    Column {

        TextField(
            value = searchText,
            onValueChange = {
                searchText = it
            },
            label = {
                Text("Найти сотрудника")
            }
        )

        LazyColumn {

            items(
                users.filter {
                    it.name.contains(
                        searchText,
                        ignoreCase = true
                    )
                }
            ) { user ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        Text(user.name)

                        Text(user.role)
                    }
                }
            }
        }
    }
}