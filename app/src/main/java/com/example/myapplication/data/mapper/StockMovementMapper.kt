package com.example.myapplication.data.mapper

import com.example.myapplication.data.local.entity.StockMovementEntity
import com.example.myapplication.data.remote.dto.StockMovementDto
import com.example.myapplication.domain.model.MovementType
import com.example.myapplication.domain.model.StockMovement

fun StockMovementDto.toDomain(): StockMovement = StockMovement(
    id = id,
    productId = productId,
    movementType = runCatching { MovementType.valueOf(movementType) }.getOrDefault(MovementType.ADJUSTMENT),
    quantity = quantity,
    userId = userId,
    userName = userName,
    timestamp = timestamp,
    isSynced = isSynced
)

fun StockMovement.toDto(): StockMovementDto = StockMovementDto(
    id = id,
    productId = productId,
    movementType = movementType.name,
    quantity = quantity,
    userId = userId,
    userName = userName,
    timestamp = timestamp,
    isSynced = isSynced
)

fun StockMovementEntity.toDomain(): StockMovement = StockMovement(
    id = id,
    productId = productId,
    movementType = runCatching { MovementType.valueOf(movementType) }.getOrDefault(MovementType.ADJUSTMENT),
    quantity = quantity,
    userId = userId,
    userName = userName,
    timestamp = timestamp,
    isSynced = isSynced
)

fun StockMovement.toEntity(): StockMovementEntity = StockMovementEntity(
    id = id,
    productId = productId,
    movementType = movementType.name,
    quantity = quantity,
    userId = userId,
    userName = userName,
    timestamp = timestamp,
    isSynced = isSynced
)