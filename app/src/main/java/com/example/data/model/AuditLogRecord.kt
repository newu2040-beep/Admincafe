package com.example.data.model

data class AuditLogRecord(
    val id: String = "",
    val restaurantId: String = "",
    val action: String = "",
    val user: String = "",
    val previousState: String = "",
    val newState: String = "",
    val reason: String = "",
    val details: String = "",
    val createdAt: String = ""
)
