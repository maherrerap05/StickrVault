package com.example.myapplication.domain.repository

import com.example.myapplication.domain.model.AppUser

interface AuthRepository {
    suspend fun login(email: String): AppUser?
    suspend fun getUsers(): List<AppUser>
}