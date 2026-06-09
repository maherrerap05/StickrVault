package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_users")
data class AppUserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val role: String,
    val lastLogin: Long
)