package com.example.myapplication.domain.repository

import com.example.myapplication.domain.model.StockMovement

interface StockMovementRepository {
    suspend fun getRecentMovements(limit: Int = 10): List<StockMovement>
    suspend fun addMovement(movement: StockMovement): StockMovement?
}