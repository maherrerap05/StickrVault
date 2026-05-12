package com.example.myapplication.presentation.home

data class HomeUiState(
    val totalProducts: Int = 0,
    val criticalStockProducts: Int = 0,
    val pendingSyncItems: Int = 0,
    val lastSyncText: String = "Sin sincronización reciente"
)