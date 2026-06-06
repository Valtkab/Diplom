package com.example.baristamessenger.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.baristamessenger.presentation.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onBackClick: () -> Unit,
    onLogout: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val error by viewModel.error.collectAsState()

    var isEditing by rememberSaveable { mutableStateOf(false) }

    // Локальные состояния для редактирования
    var firstName by rememberSaveable { mutableStateOf("") }
    var lastName by rememberSaveable { mutableStateOf("") }
    var nickname by rememberSaveable { mutableStateOf("") }
    var birthDate by rememberSaveable { mutableStateOf("") }
    var aboutMe by rememberSaveable { mutableStateOf("") }

    // Загружаем данные из ViewModel в локальные переменные
    LaunchedEffect(userProfile) {
        userProfile?.let {
            firstName = it.firstName
            lastName = it.lastName
            nickname = it.nickname
            birthDate = it.birthDate
            aboutMe = it.aboutMe
        }
    }

    // Snackbar для ошибок
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(error) {
        error?.let {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.clearError()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Редактирование" else "Профиль",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
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
                    // Кнопка Сохранить / Редактировать
                    IconButton(
                        enabled = !isSaving,
                        onClick = {
                            if (isEditing) {
                                viewModel.saveUserProfile(
                                    firstName = firstName,
                                    lastName = lastName,
                                    nickname = nickname,
                                    birthDate = birthDate,
                                    aboutMe = aboutMe
                                ) {
                                    isEditing = false
                                }
                            } else {
                                isEditing = true
                            }
                        }
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
                            )
                        } else {
                            Icon(
                                imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Settings,
                                contentDescription = if (isEditing) "Сохранить" else "Редактировать",
                                tint = if (isEditing) Color(0xFF4CAF50) else Color.Gray
                            )
                        }
                    }

                    // Меню с выходом
                    var showMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Еще",
                                tint = Color.Gray
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(Color(0xFF2C2C2C))
                        ) {
                            DropdownMenuItem(
                                text = { Text("Выйти из аккаунта", color = Color.Red) },
                                onClick = {
                                    showMenu = false
                                    viewModel.logout { onLogout() }
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black
                )
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFE65100))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // --- БЛОК АВАТАРА И ИМЕНИ ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(86.dp)) {
                        AsyncImage(
                            model = userProfile?.imageUrl?.ifEmpty { "https://placeholder.com/user_avatar.jpg" }
                                ?: "https://placeholder.com/user_avatar.jpg",
                            contentDescription = "Аватар профиля",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2C2C2C)),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .align(Alignment.BottomEnd)
                                .clip(CircleShape)
                                .background(Color(0xFFE65100))
                                .border(2.dp, Color.Black, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(" ", fontSize = 10.sp)
                        }
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        if (isEditing) {
                            OutlinedTextField(
                                value = firstName,
                                onValueChange = { firstName = it },
                                label = { Text("Имя") },
                                placeholder = { Text("Введите имя", color = Color.DarkGray) },
                                colors = textFieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = lastName,
                                onValueChange = { lastName = it },
                                label = { Text("Фамилия") },
                                placeholder = { Text("Введите фамилию", color = Color.DarkGray) },
                                colors = textFieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Text(
                                text = if (firstName.isNotEmpty() || lastName.isNotEmpty())
                                    "$firstName $lastName".trim()
                                else "Имя не указано",
                                color = if (firstName.isEmpty() && lastName.isEmpty()) Color.Gray else Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // --- РАЗДЕЛ: ЛИЧНЫЕ ДАННЫЕ (НИК И ДР) ---
                if (isEditing) {
                    OutlinedTextField(
                        value = nickname,
                        onValueChange = { nickname = it },
                        label = { Text("Имя пользователя (никнейм)") },
                        placeholder = { Text("@username", color = Color.DarkGray) },
                        colors = textFieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = birthDate,
                        onValueChange = { birthDate = it },
                        label = { Text("Дата рождения") },
                        placeholder = { Text("Например: 02 дек. 2005", color = Color.DarkGray) },
                        colors = textFieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = "Личные данные",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF161616), shape = RoundedCornerShape(16.dp))
                            .border(1.dp, Color(0xFF262626), shape = RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        InfoRow(label = "Имя пользователя", value = nickname.ifEmpty { "Не указано" })
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF262626)))
                        InfoRow(label = "Дата рождения", value = birthDate.ifEmpty { "Не указана" })
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // --- РАЗДЕЛ: О СЕБЕ ---
                Text(
                    text = "О себе",
                    color = if (isEditing) Color.Gray else Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (isEditing) {
                    OutlinedTextField(
                        value = aboutMe,
                        onValueChange = { aboutMe = it },
                        placeholder = { Text("Расскажите немного о себе...", color = Color.DarkGray) },
                        colors = textFieldColors(),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF161616), shape = RoundedCornerShape(16.dp))
                            .border(1.dp, Color(0xFF262626), shape = RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = aboutMe.ifEmpty { "Здесь пока ничего нет. Нажмите на шестеренку, чтобы добавить описание!" },
                            color = if (aboutMe.isEmpty()) Color.Gray else Color(0xFFE0E0E0),
                            fontSize = 15.sp,
                            lineHeight = 22.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            color = if (value == "Не указано" || value == "Не указана") Color.Gray else Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color(0xFFE65100),
    unfocusedBorderColor = Color(0xFF262626),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedContainerColor = Color(0xFF161616),
    unfocusedContainerColor = Color(0xFF161616),
    focusedLabelColor = Color(0xFFE65100),
    unfocusedLabelColor = Color.Gray
)