package com.example.myapplication.domain.model

data class AppUser(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole
)