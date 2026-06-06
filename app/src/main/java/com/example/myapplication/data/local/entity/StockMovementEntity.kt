package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stock_movements")
data class StockMovementEntity(
    @PrimaryKey val id: String,
    val productId: String,
    val movementType: String,
    val quantity: Int,
    val userId: String,
    val userName: String,
    val timestamp: Long,
    val isSynced: Boolean
)