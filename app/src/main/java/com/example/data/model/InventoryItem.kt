package com.example.data.model

data class InventoryItem(
    val id: String = "",
    val restaurantId: String = "",
    val ingredientName: String = "",
    val currentStock: Double = 0.0,
    val unit: String = "kg", // kg, l, pcs, packs
    val minStock: Double = 5.0,
    val supplier: String = "",
    val costPerUnit: Double = 0.0,
    val status: String = "GOOD", // GOOD, LOW_STOCK
    val updatedAt: String = ""
)
