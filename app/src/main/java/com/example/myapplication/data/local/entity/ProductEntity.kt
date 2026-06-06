package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val description: String,
    val currentStock: Int,
    val minimumStock: Int,
    val imageUrl: String?,
    val ocrIdentifier: String?,
    val lastUpdated: Long,
    val isSynced: Boolean
)