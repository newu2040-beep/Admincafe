package com.example.data.model

data class Restaurant(
    val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val phone: String = "",
    val address: String = "",
    val city: String = "",
    val currency: String = "Rs.",
    val taxRate: Double = 13.0,
    val serviceCharge: Double = 10.0,
    val setupCompleted: Boolean = false,
    val createdAt: String = "",
    val updatedAt: String = ""
)
