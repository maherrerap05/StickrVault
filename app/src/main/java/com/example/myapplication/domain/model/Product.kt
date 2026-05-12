package com.example.myapplication.domain.model

data class Product(
    val id: String,
    val name: String,
    val category: ProductCategory,
    val description: String,
    val currentStock: Int,
    val minimumStock: Int,
    val imageUrl: String?,
    val ocrIdentifier: String?,
    val lastUpdated: Long,
    val isSynced: Boolean = false
)