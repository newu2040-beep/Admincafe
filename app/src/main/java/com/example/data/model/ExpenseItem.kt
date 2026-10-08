package com.example.data.model

data class ExpenseItem(
    val id: String = "",
    val restaurantId: String = "",
    val amount: Double = 0.0,
    val category: String = "INGREDIENTS", // INGREDIENTS, RENT, ELECTRICITY, SALARIES, MAINTENANCE, OTHER
    val description: String = "",
    val date: String = "",
    val addedBy: String = "",
    val createdAt: String = ""
)
