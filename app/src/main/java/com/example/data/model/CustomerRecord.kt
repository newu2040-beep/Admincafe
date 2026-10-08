package com.example.data.model

data class CustomerRecord(
    val id: String = "",
    val restaurantId: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val orderCount: Int = 0,
    val totalSpent: Double = 0.0,
    val lastOrderDate: String = "",
    val favoriteItems: String = "",
    val createdAt: String = ""
)
