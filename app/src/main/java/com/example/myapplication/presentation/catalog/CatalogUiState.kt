package com.example.myapplication.presentation.catalog

import com.example.myapplication.domain.model.Product
import com.example.myapplication.domain.model.ProductCategory

sealed class CatalogUiState {
    object Loading : CatalogUiState()

    data class Success(
        val products: List<Product>,
        val activeFilter: ProductCategory? = null,
        val searchQuery: String = ""
    ) : CatalogUiState()

    data class Error(
        val message: String,
        val canRetry: Boolean = true
    ) : CatalogUiState()

    object Empty : CatalogUiState()
}