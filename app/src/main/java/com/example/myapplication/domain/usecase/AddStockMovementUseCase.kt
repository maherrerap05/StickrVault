package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.model.StockMovement
import com.example.myapplication.domain.repository.StockMovementRepository

class AddStockMovementUseCase(private val repository: StockMovementRepository) {
    suspend operator fun invoke(movement: StockMovement): StockMovement? =
        repository.addMovement(movement)
}