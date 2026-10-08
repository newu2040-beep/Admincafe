package com.example.data.model

data class OrderItem(
    val id: String = "",
    val foodItemId: String = "",
    val name: String = "",
    val variantName: String = "",
    val quantity: Int = 1,
    val unitPrice: Double = 0.0,
    val totalPrice: Double = 0.0,
    val stationId: String = "",
    val stationName: String = "",
    val status: String = "PREPARING", // PREPARING, READY, SERVED, CANCELLED
    val cancelReason: String = "",
    val cancelledBy: String = "",
    val cancelledAt: String = ""
)

data class RestaurantOrder(
    val id: String = "",
    val orderNumber: String = "",
    val restaurantId: String = "",
    val tableId: String = "",
    val tableNumber: String = "",
    val status: String = "NEW", // DRAFT, NEW, ACCEPTED, PREPARING, READY, SERVED, COMPLETED, CANCELLED
    val paymentStatus: String = "PENDING", // PENDING, PAID, REFUNDED
    val paymentMethod: String = "CASH", // CASH, CARD, QR, OTHER
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val tax: Double = 0.0,
    val serviceCharge: Double = 0.0,
    val total: Double = 0.0,
    val itemsJson: String = "[]",
    val cancelReason: String = "",
    val cancelledBy: String = "",
    val serverName: String = "",
    val notes: String = "",
    val createdAt: String = "",
    val updatedAt: String = ""
)
