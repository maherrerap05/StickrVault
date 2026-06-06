package com.example.myapplication.data.repository

import com.example.myapplication.data.mapper.toDomain
import com.example.myapplication.data.remote.api.SupabaseApiService
import com.example.myapplication.domain.model.AppUser
import com.example.myapplication.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepositoryImpl(
    private val apiService: SupabaseApiService
) : AuthRepository {

    override suspend fun login(email: String): AppUser? = withContext(Dispatchers.IO) {
        runCatching {
            apiService.getUserByEmail("eq.$email").firstOrNull()?.toDomain()
        }.getOrNull()
    }
}