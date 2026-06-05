package com.example.baristamessenger.data.model

data class ShiftRequest(
    val id: String = "",             // Уникальный ID самого запроса
    val baristaId: String = "",      // ID того, кто создал запрос
    val baristaName: String = "",    // Имя бариста (которое мы передавали в Workspace)
    val shiftDate: String = "",      // Дата смены, которую хотят отдать/взять
    val comment: String = "",        // Комментарий (например, "Ищу замену на утро")
    val createdAt: Long = System.currentTimeMillis() // Время создания для сортировки
)