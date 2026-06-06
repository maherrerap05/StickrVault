package com.example.myapplication.data.remote.dto

import com.google.gson.annotations.SerializedName

data class StockMovementDto(
    @SerializedName("id") val id: String,
    @SerializedName("product_id") val productId: String,
    @SerializedName("movement_type") val movementType: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("user_id") val userId: String,
    @SerializedName("user_name") val userName: String,
    @SerializedName("timestamp") val timestamp: Long,
    @SerializedName("is_synced") val isSynced: Boolean
)