package com.example.data.model

data class StaffMember(
    val id: String = "",
    val restaurantId: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val role: String = "WAITER", // OWNER, MANAGER, WAITER, CHEF, CASHIER, KITCHEN_STAFF
    val status: String = "ACTIVE", // ACTIVE, SUSPENDED, DISABLED
    val assignedStation: String = "",
    val createdAt: String = "",
    val updatedAt: String = ""
)
