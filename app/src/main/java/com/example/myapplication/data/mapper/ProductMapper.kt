package com.example.myapplication.data.mapper

import com.example.myapplication.data.remote.dto.ProductDto
import com.example.myapplication.domain.model.Product
import com.example.myapplication.domain.model.ProductCategory

fun ProductDto.toDomain(): Product {
    return Product(
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
}

private fun String.toProductCategory(): ProductCategory {
    return when (this.uppercase()) {

        "CROMO",
        "CROMOS",
        "STICKER",
        "STICKERS",
        "INDIVIDUAL" -> ProductCategory.STICKER_INDIVIDUAL

        "PACK",
        "PACKS",
        "SOBRE",
        "SOBRES" -> ProductCategory.STICKER_PACK

        "ALBUM",
        "ÁLBUM",
        "ALBUMES",
        "ÁLBUMES" -> ProductCategory.ALBUM

        "BOX",
        "CAJA",
        "STICKER_BOX" -> ProductCategory.STICKER_BOX

        "PELUCHE",
        "PELUCHES",
        "PLUSH" -> ProductCategory.PLUSH

        "BALON",
        "BALÓN",
        "BALONES",
        "BALL" -> ProductCategory.BALL

        "SPECIAL",
        "SPECIAL_EDITION",
        "EDICION_ESPECIAL",
        "EDICIÓN_ESPECIAL" -> ProductCategory.SPECIAL_EDITION

        else -> ProductCategory.STICKER_INDIVIDUAL
    }
}