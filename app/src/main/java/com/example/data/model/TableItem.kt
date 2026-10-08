package com.example.data.model

data class TableItem(
    val id: String = "",
    val restaurantId: String = "",
    val tableNumber: String = "",
    val capacity: Int = 4,
    val floor: String = "Ground Floor",
    val status: String = "AVAILABLE", // AVAILABLE, OCCUPIED, RESERVED, CLEANING, DISABLED
    val qrCode: String = "",
    val currentOrderId: String = "",
    val createdAt: String = "",
    val updatedAt: String = ""
)
