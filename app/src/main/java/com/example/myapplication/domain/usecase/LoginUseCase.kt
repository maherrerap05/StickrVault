package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.model.AppUser
import com.example.myapplication.domain.repository.AuthRepository

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String): AppUser? = repository.login(email)
}