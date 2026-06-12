package com.example.myapplication.data.repository

import com.example.myapplication.data.local.dao.ProductDao
import com.example.myapplication.data.local.dao.StockMovementDao
import com.example.myapplication.data.local.model.StockMovementWithProductName
import com.example.myapplication.data.mapper.toDomain
import com.example.myapplication.data.mapper.toEntity
import com.example.myapplication.data.remote.api.SupabaseApiService
import com.example.myapplication.data.remote.dto.StockMovementDto
import com.example.myapplication.domain.model.MovementType
import com.example.myapplication.domain.model.StockMovement
import com.example.myapplication.domain.repository.StockMovementRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StockMovementRepositoryImpl(
    private val apiService: SupabaseApiService,
    private val dao: StockMovementDao,
    private val productDao: ProductDao
) : StockMovementRepository {

    override suspend fun getRecentMovements(limit: Int): List<StockMovement> =
        withContext(Dispatchers.IO) {
            try {
                val remote = apiService.getStockMovements(limit = limit).map { it.toModel() }
                dao.upsertMovements(
                    remote.map { movement ->
                        movement.toEntity().copy(
                            productName = movement.productName ?: resolveProductName(movement.productId)
                        )
                    }
                )
            } catch (_: Exception) { }
            dao.getRecentMovementsWithProductName(limit).map { it.toModel() }
        }

    override suspend fun addMovement(movement: StockMovement): StockMovement? =
        withContext(Dispatchers.IO) {
            val localMovement = movement.copy(
                productName = movement.productName ?: resolveProductName(movement.productId)
            )
            dao.upsertMovement(localMovement.toEntity())
            runCatching {
                val result = apiService.addStockMovement(localMovement.toDto())
                    .firstOrNull()
                    ?.toModel()
                    ?.copy(productName = localMovement.productName)
                result?.let { dao.upsertMovement(it.toEntity()) }
                result ?: localMovement
            }.getOrElse { localMovement }
        }

    private suspend fun resolveProductName(productId: String): String? {
        productDao.getProductById(productId)?.name?.let { return it }

        return runCatching {
            apiService.getProductById("eq.$productId")
                .firstOrNull()
                ?.toDomain()
                ?.also { productDao.upsertProduct(it.toEntity()) }
                ?.name
        }.getOrNull()
    }

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

    private fun StockMovementWithProductName.toModel() = StockMovement(
        id           = id,
        productId    = productId,
        movementType = runCatching { MovementType.valueOf(movementType) }
            .getOrDefault(MovementType.ADJUSTMENT),
        quantity     = quantity,
        userId       = userId,
        userName     = userName,
        timestamp    = timestamp,
        isSynced     = isSynced,
        productName  = productName
    )

    private fun StockMovement.toEntity() = com.example.myapplication.data.local.entity.StockMovementEntity(
        id           = id,
        productId    = productId,
        movementType = movementType.name,
        quantity     = quantity,
        userId       = userId,
        userName     = userName,
        timestamp    = timestamp,
        isSynced     = isSynced,
        productName  = productName
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
