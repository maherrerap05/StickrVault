package com.example.myapplication.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ProductDto(

    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("category")
    val category: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("current_stock")
    val currentStock: Int,

    @SerializedName("minimum_stock")
    val minimumStock: Int,

    @SerializedName("image_url")
    val imageUrl: String?,

    @SerializedName("ocr_identifier")
    val ocrIdentifier: String?,

    @SerializedName("last_updated")
    val lastUpdated: Long,

    @SerializedName("is_synced")
    val isSynced: Boolean
)