package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.model.StockMovement
import com.example.myapplication.domain.repository.StockMovementRepository

class GetStockMovementsUseCase(private val repository: StockMovementRepository) {
    suspend operator fun invoke(limit: Int = 10): List<StockMovement> =
        repository.getRecentMovements(limit)
}