package com.example.baristamessenger.domain.model

data class Ingredient(
    val id: String,
    val name: String,
    val amount: Int,       // Объем/вес (например, 150 мл или 18 г)
    val price: Double      // Цена за этот объем
)