package com.example.myapplication.presentation.scanner

import com.example.myapplication.domain.model.Product

sealed class ScannerUiState {
    object Idle : ScannerUiState()
    object Scanning : ScannerUiState()
    data class ProductDetected(val identifier: String) : ScannerUiState()
    data class ProductFound(val product: Product) : ScannerUiState()
    data class ProductNotFound(val identifier: String) : ScannerUiState()
    data class Error(val message: String) : ScannerUiState()
}