package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.model.AppUser
import com.example.myapplication.domain.repository.AuthRepository

class GetUsersUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(): List<AppUser> = repository.getUsers()
}