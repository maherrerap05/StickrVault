package com.example.myapplication.data.repository

import com.example.myapplication.data.local.dao.StockMovementDao
import com.example.myapplication.data.local.entity.StockMovementEntity
import com.example.myapplication.data.remote.api.SupabaseApiService
import com.example.myapplication.data.remote.dto.StockMovementDto
import com.example.myapplication.domain.model.MovementType
import com.example.myapplication.domain.model.StockMovement
import com.example.myapplication.domain.repository.StockMovementRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StockMovementRepositoryImpl(
    private val apiService: SupabaseApiService,
    private val dao: StockMovementDao
) : StockMovementRepository {

    override suspend fun getRecentMovements(limit: Int): List<StockMovement> =
        withContext(Dispatchers.IO) {
            try {
                val remote = apiService.getStockMovements(limit = limit).map { it.toModel() }
                dao.upsertMovements(remote.map { it.toEntity() })
                remote
            } catch (e: Exception) {
                dao.getRecentMovements(limit).map { it.toModel() }
            }
        }

    override suspend fun addMovement(movement: StockMovement): StockMovement? =
        withContext(Dispatchers.IO) {
            runCatching {
                val result = apiService.addStockMovement(movement.toDto()).firstOrNull()?.toModel()
                result?.let { dao.upsertMovement(it.toEntity()) }
                result
            }.getOrNull()
        }

    // ── Mappers privados para evitar ambigüedad de imports ──────────────
    private fun StockMovementDto.toModel() = StockMovement(
        id           = id,
        productId    = productId,
        movementType = runCatching { MovementType.valueOf(movementType) }
            .getOrDefault(MovementType.ADJUSTMENT),
        quantity     = quantity,
        userId       = userId,
        userName     = userName,
        timestamp    = timestamp,
        isSynced     = isSynced
    )

    private fun StockMovementEntity.toModel() = StockMovement(
        id           = id,
        productId    = productId,
        movementType = runCatching { MovementType.valueOf(movementType) }
            .getOrDefault(MovementType.ADJUSTMENT),
        quantity     = quantity,
        userId       = userId,
        userName     = userName,
        timestamp    = timestamp,
        isSynced     = isSynced
    )

    private fun StockMovement.toEntity() = StockMovementEntity(
        id           = id,
        productId    = productId,
        movementType = movementType.name,
        quantity     = quantity,
        userId       = userId,
        userName     = userName,
        timestamp    = timestamp,
        isSynced     = isSynced
    )

    private fun StockMovement.toDto() = StockMovementDto(
        id           = id,
        productId    = productId,
        movementType = movementType.name,
        quantity     = quantity,
        userId       = userId,
        userName     = userName,
        timestamp    = timestamp,
        isSynced     = isSynced
    )
}