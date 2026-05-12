package com.example.myapplication.domain.model

data class StockMovement(
    val id: String,
    val productId: String,
    val movementType: MovementType,
    val quantity: Int,
    val userId: String,
    val userName: String,
    val timestamp: Long,
    val isSynced: Boolean = false
)