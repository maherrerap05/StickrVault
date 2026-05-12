package com.example.myapplication.presentation.reports

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ReportsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        ReportsUiState(
            totalProducts = 3,
            totalMovements = 12,
            criticalStockProducts = 1,
            mostStockedProductName = "Cromo Messi"
        )
    )

    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()
}