package com.example.myapplication.data.local.model

import androidx.room.ColumnInfo

data class StockMovementWithProductName(
    val id: String,
    val productId: String,
    val movementType: String,
    val quantity: Int,
    val userId: String,
    val userName: String,
    val timestamp: Long,
    val isSynced: Boolean,
    @ColumnInfo(name = "productName") val productName: String?
)
