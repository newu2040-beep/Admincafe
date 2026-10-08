package com.example.data.model

import com.google.firebase.firestore.PropertyName

data class FoodItem(
    val id: String = "",
    val restaurantId: String = "",
    val categoryId: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val prepTimeMinutes: Int = 10,
    @get:PropertyName("isAvailable") @set:PropertyName("isAvailable")
    var isAvailable: Boolean = true,
    val stationId: String = "",
    val stationName: String = "",
    val variants: String = "",
    val addons: String = "",
    val ingredients: String = "",
    val createdAt: String = "",
    val updatedAt: String = ""
)
