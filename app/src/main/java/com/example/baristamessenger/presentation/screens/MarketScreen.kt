package com.example.baristamessenger.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class MarketItem(
    val title: String,
    val description: String,
    val price: String,
    val locationOrDate: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketScreen(
    onBackClick: () -> Unit
) {
    val darkBackground = Color(0xFF121111)
    val cardBackground = Color(0xFF1E1B1B)
    val accentBrown = Color(0xFF845A38)
    val secondaryText = Color(0xFF8E8888)

    var selectedTab by remember { mutableStateOf("Все") }
    val tabs = listOf("Все", "Продаю", "Покупаю")

    // ЗАМЕНЕНО: Используем стандартные доступные иконки ShoppingCart и Build
    val itemsList = listOf(
        MarketItem("Питчер 600 мл", "Acaia, черный", "1 800 ₽", "Сегодня, 10:12", Icons.Default.ShoppingCart),
        MarketItem("Темпер 58 мм", "Весна, дерево", "2 500 ₽", "Вчера, 21:45", Icons.Default.ShoppingCart),
        MarketItem("Кофемолка Fiorenzato F64", "Б/у, отличное состояние", "35 000 ₽", "Вчера, 18:30", Icons.Default.Build),
        MarketItem("Весы Timemore Black Mirror", "Практически новые", "8 000 ₽", "2 дн. назад", Icons.Default.ShoppingCart)
    )

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
                title = { Text("Барахолка", fontSize = 20.sp, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { }) { Icon(Icons.Default.Search, contentDescription = "Поиск") }
                    IconButton(onClick = { }) { Icon(Icons.Default.MoreVert, contentDescription = "Меню") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { },
                containerColor = accentBrown,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                // ИСПРАВЛЕНО: убрали опечатку 'tragic'
                modifier = Modifier.padding(bottom = 8.dp, end = 8.dp)
            ) {
                // ИСПРАВЛЕНО: Размер иконки теперь передается через Modifier
                Icon(Icons.Default.Add, contentDescription = "Добавить", modifier = Modifier.size(28.dp))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEach { tab ->
                    val isSelected = selectedTab == tab
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) accentBrown.copy(alpha = 0.4f) else Color.Transparent)
                            .clickable { selectedTab = tab }
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            color = if (isSelected) Color(0xFFE6A15C) else secondaryText,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(itemsList) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = cardBackground),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF141212)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    tint = accentBrown,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(item.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = secondaryText)
                                }
                                Text(item.description, color = secondaryText, fontSize = 13.sp, modifier = Modifier.padding(top = 2.dp))

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Text(item.price, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text(item.locationOrDate, color = secondaryText, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}