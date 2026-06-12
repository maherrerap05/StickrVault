package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.myapplication.data.local.entity.StockMovementEntity
import com.example.myapplication.data.local.model.StockMovementWithProductName

@Dao
interface StockMovementDao {

    @Query("SELECT * FROM stock_movements ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMovements(limit: Int = 10): List<StockMovementEntity>

    @Query("""
        SELECT m.id, m.productId, m.movementType, m.quantity, m.userId, m.userName,
               m.timestamp, m.isSynced, p.name AS productName
        FROM stock_movements m
        LEFT JOIN products p ON m.productId = p.id
        ORDER BY m.timestamp DESC
        LIMIT :limit
    """)
    suspend fun getRecentMovementsWithProductName(limit: Int): List<StockMovementWithProductName>

    @Upsert
    suspend fun upsertMovements(movements: List<StockMovementEntity>)

    @Upsert
    suspend fun upsertMovement(movement: StockMovementEntity)
}