package com.example.myapplication.presentation.scanner

import com.example.myapplication.domain.model.Product

sealed class ScannerUiState {
    object Idle : ScannerUiState()
    object CameraReady : ScannerUiState()
    object ProcessingCapture : ScannerUiState()
    data class Searching(val code: String) : ScannerUiState()
    data class ProductFound(val product: Product, val code: String) : ScannerUiState()
    data class ProductNotFound(val code: String) : ScannerUiState()
    data class CodeNotRecognized(val rawText: String) : ScannerUiState()
    data class Error(val message: String) : ScannerUiState()
}
