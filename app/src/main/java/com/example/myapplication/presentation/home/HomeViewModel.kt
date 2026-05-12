package com.example.myapplication.presentation.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(
            totalProducts = 3,
            criticalStockProducts = 1,
            pendingSyncItems = 1,
            lastSyncText = "Datos locales de prueba"
        )
    )

    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
}