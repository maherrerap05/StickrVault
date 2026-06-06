package com.example.myapplication.presentation.home

import com.example.myapplication.domain.model.AppUser
import com.example.myapplication.domain.model.StockMovement

data class HomeUiState(
    val totalProducts: Int = 0,
    val criticalStockProducts: Int = 0,
    val pendingSyncItems: Int = 0,
    val lastSyncText: String = "Sin sincronización reciente",
    val recentMovements: List<StockMovement> = emptyList(),
    val users: List<AppUser> = emptyList()
)