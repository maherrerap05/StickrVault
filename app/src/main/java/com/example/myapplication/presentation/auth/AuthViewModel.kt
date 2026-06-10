package com.example.myapplication.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.AppUser
import com.example.myapplication.domain.repository.AuthRepository
import com.example.myapplication.domain.usecase.LoginUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val loginUseCase: LoginUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _currentUser = MutableStateFlow<AppUser?>(null)
    val currentUser: StateFlow<AppUser?> = _currentUser.asStateFlow()

    private val _isSessionReady = MutableStateFlow(false)
    val isSessionReady: StateFlow<Boolean> = _isSessionReady.asStateFlow()

    init {
        restoreSession()
    }

    private fun restoreSession() {
        viewModelScope.launch {
            _currentUser.value = authRepository.getSavedSession()
            _isSessionReady.value = true
        }
    }

    fun login(email: String) {
        if (email.isBlank()) {
            _uiState.value = AuthUiState.Error("Ingresa un correo válido")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val user = loginUseCase(email.trim())
            if (user != null) {
                _currentUser.value = user
                _uiState.value = AuthUiState.Success(user)
            } else {
                _uiState.value = AuthUiState.Error("Usuario no encontrado. Verifica tu correo.")
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _uiState.value = AuthUiState.Idle
        viewModelScope.launch {
            authRepository.clearSession()
        }
    }
}
