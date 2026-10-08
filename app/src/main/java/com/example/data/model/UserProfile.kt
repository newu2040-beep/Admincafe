package com.example.data.model

data class UserProfile(
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val restaurantId: String = "",
    val role: String = "OWNER", // OWNER, MANAGER, WAITER, CHEF, CASHIER
    val createdAt: String = ""
)
