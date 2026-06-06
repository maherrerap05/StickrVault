package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.myapplication.data.local.entity.StockMovementEntity

@Dao
interface StockMovementDao {

    @Query("SELECT * FROM stock_movements ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMovements(limit: Int = 10): List<StockMovementEntity>

    @Upsert
    suspend fun upsertMovements(movements: List<StockMovementEntity>)

    @Upsert
    suspend fun upsertMovement(movement: StockMovementEntity)
}