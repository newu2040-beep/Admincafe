package com.example.data.model

import com.google.firebase.firestore.PropertyName

data class KitchenStation(
    val id: String = "",
    val restaurantId: String = "",
    val name: String = "",
    val description: String = "",
    @get:PropertyName("isActive") @set:PropertyName("isActive")
    var isActive: Boolean = true,
    val createdAt: String = "",
    val updatedAt: String = ""
)
