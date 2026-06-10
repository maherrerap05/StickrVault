package com.example.myapplication.presentation.catalog

import com.example.myapplication.domain.model.ProductCategory

data class AddProductDraft(
    val name: String = "",
    val category: ProductCategory = ProductCategory.STICKER_INDIVIDUAL,
    val stock: String = "",
    val minStock: String = "15",
    val ocrId: String = "",
    val wasVerified: Boolean = false,
    val existingProductId: String? = null
) {
    val isEmpty: Boolean
        get() = name.isBlank() &&
            stock.isBlank() &&
            ocrId.isBlank() &&
            !wasVerified &&
            category == ProductCategory.STICKER_INDIVIDUAL &&
            minStock == "15"
}
