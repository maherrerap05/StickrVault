package com.example.myapplication.presentation.auth

import com.example.myapplication.domain.model.AppUser

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(
        val user: AppUser,
        val isOffline: Boolean = false
    ) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}