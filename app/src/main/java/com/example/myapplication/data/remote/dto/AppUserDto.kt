package com.example.myapplication.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AppUserDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("role") val role: String
)