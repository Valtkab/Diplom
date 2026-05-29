package com.example.baristamessenger.domain.model

data class ShiftRequest(
    val id: String,
    val date: String,
    val time: String,
    val location: String,
    val authorName: String,
    val comment: String,
    val isOpened: Boolean = true
)