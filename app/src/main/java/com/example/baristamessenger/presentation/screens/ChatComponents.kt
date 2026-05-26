package com.example.baristamessenger.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.baristamessenger.domain.model.Message
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun MessageBubble(message: Message) {
    val isMyMessage = message.senderId == "current_barista_id"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = if (isMyMessage) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        if (message.isRecipe) {

            RecipeCard(message = message)
        } else {
            Box(
                modifier = Modifier
                    .background(
                        color = if (isMyMessage) Color(0xFF4E342E) else Color(0xFFE0E0E0),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = message.text,
                    color = if (isMyMessage) Color.White else Color.Black,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun RecipeCard(message: Message) {
    Card(
        modifier = Modifier.width(250.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF3E0)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "НОВЫЙ РЕЦЕПТ",
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE65100),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp)) // Отступ вниз

            // Тело рецепта
            Text(
                text = message.text,
                color = Color.Black,
                fontSize = 15.sp
            )
        }
    }
}
    @Preview(showBackground = true)
    @Composable
    fun MessageBubblePreview() {
        val testMessage = Message(
            id = "1",
            chatId = "chat_1",
            senderId = "another_barista",
            text = "Привет! Держи ТТК на новый латте Сингапур:\nЭспрессо: 30 мл\nМолоко: 200 мл\nСироп карамель: 15 мл\nЛемонграсс: 2 г",
            timestamp = System.currentTimeMillis(),
            isRecipe = true
        )

        MessageBubble(message = testMessage)
}