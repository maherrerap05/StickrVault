package com.example.myapplication.data.mapper

import com.example.myapplication.data.local.entity.ProductEntity
import com.example.myapplication.data.remote.dto.ProductDto
import com.example.myapplication.domain.model.Product
import com.example.myapplication.domain.model.ProductCategory

// DTO → Domain
fun ProductDto.toDomain(): Product = Product(
    id = id,
    name = name,
    category = category.toProductCategory(),
    description = description,
    currentStock = currentStock,
    minimumStock = minimumStock,
    imageUrl = imageUrl,
    ocrIdentifier = ocrIdentifier,
    lastUpdated = System.currentTimeMillis(),
    isSynced = true
)

// Entity → Domain
fun ProductEntity.toDomain(): Product = Product(
    id = id,
    name = name,
    category = category.toProductCategory(),
    description = description,
    currentStock = currentStock,
    minimumStock = minimumStock,
    imageUrl = imageUrl,
    ocrIdentifier = ocrIdentifier,
    lastUpdated = lastUpdated,
    isSynced = isSynced
)

// Domain → Entity
fun Product.toEntity(): ProductEntity = ProductEntity(
    id = id,
    name = name,
    category = category.name,
    description = description,
    currentStock = currentStock,
    minimumStock = minimumStock,
    imageUrl = imageUrl,
    ocrIdentifier = ocrIdentifier,
    lastUpdated = lastUpdated,
    isSynced = isSynced
)

private fun String.toProductCategory(): ProductCategory = when (this.uppercase()) {
    "CROMO", "CROMOS", "STICKER", "STICKERS",
    "INDIVIDUAL", "STICKER_INDIVIDUAL"          -> ProductCategory.STICKER_INDIVIDUAL
    "PACK", "PACKS", "SOBRE", "SOBRES",
    "STICKER_PACK"                              -> ProductCategory.STICKER_PACK
    "ALBUM", "ÁLBUM", "ALBUMES", "ÁLBUMES"      -> ProductCategory.ALBUM
    "BOX", "CAJA", "STICKER_BOX"               -> ProductCategory.STICKER_BOX
    "PELUCHE", "PELUCHES", "PLUSH"             -> ProductCategory.PLUSH
    "BALON", "BALÓN", "BALONES", "BALL"         -> ProductCategory.BALL
    "SPECIAL", "SPECIAL_EDITION",
    "EDICION_ESPECIAL", "EDICIÓN_ESPECIAL"      -> ProductCategory.SPECIAL_EDITION
    else                                        -> ProductCategory.STICKER_INDIVIDUAL
}