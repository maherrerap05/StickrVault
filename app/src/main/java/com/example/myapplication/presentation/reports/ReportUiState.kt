package com.example.myapplication.presentation.reports

data class ReportsUiState(
    val totalProducts: Int = 0,
    val totalMovements: Int = 0,
    val criticalStockProducts: Int = 0,
    val mostStockedProductName: String = "Sin datos"
)