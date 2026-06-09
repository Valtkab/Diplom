package com.example.baristamessenger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.baristamessenger.presentation.screens.OrderItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class OrderViewModel : ViewModel() {
    private val _orderItems = MutableStateFlow<List<OrderItem>>(emptyList())
    val orderItems = _orderItems.asStateFlow()

    fun addOrder(item: OrderItem) {
        _orderItems.value = _orderItems.value + item
    }

    fun removeOrder(id: Long) {
        // Фильтруем список, оставляя все, кроме того, что мы "выполнили"
        _orderItems.value = _orderItems.value.filter { it.id != id }
    }
}