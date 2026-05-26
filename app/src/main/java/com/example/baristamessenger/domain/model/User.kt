package com.example.baristamessenger.domain.model

data class User(
    val id: String,
    val name: String,
    val imageUrl: String,
    val role: String,
    val coffeeShop: String
)