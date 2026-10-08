package com.example.data.model

import com.google.firebase.firestore.PropertyName

data class Category(
    val id: String = "",
    val restaurantId: String = "",
    val name: String = "",
    val description: String = "",
    val displayOrder: Int = 0,
    @get:PropertyName("isActive") @set:PropertyName("isActive")
    var isActive: Boolean = true,
    val createdAt: String = "",
    val updatedAt: String = ""
)
