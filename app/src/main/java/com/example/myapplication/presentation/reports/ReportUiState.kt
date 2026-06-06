package com.example.myapplication.presentation.reports

import com.example.myapplication.domain.model.StockMovement

data class ReportsUiState(
    val totalProducts: Int = 0,
    val totalMovements: Int = 0,
    val criticalStockProducts: Int = 0,
    val mostStockedProductName: String = "Sin datos",
    val recentMovements: List<StockMovement> = emptyList()
)