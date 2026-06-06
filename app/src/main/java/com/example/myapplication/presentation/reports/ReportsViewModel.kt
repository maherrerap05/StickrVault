package com.example.myapplication.presentation.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.usecase.GetProductsUseCase
import com.example.myapplication.domain.usecase.GetStockMovementsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReportsViewModel(
    private val getProductsUseCase: GetProductsUseCase,
    private val getStockMovementsUseCase: GetStockMovementsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    init { loadReports() }

    fun loadReports() {
        viewModelScope.launch {
            try {
                val products  = getProductsUseCase()
                val movements = getStockMovementsUseCase(limit = 10)
                _uiState.value = ReportsUiState(
                    totalProducts          = products.size,
                    totalMovements         = movements.size,
                    criticalStockProducts  = products.count { it.currentStock <= it.minimumStock },
                    mostStockedProductName = products.maxByOrNull { it.currentStock }?.name ?: "Sin datos",
                    recentMovements        = movements
                )
            } catch (e: Exception) {
                _uiState.value = ReportsUiState()
            }
        }
    }
}