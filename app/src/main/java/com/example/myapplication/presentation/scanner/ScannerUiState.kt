package com.example.myapplication.presentation.scanner

sealed class ScannerUiState {
    object Idle : ScannerUiState()
    object Scanning : ScannerUiState()

    data class ProductDetected(
        val identifier: String
    ) : ScannerUiState()

    data class Error(
        val message: String
    ) : ScannerUiState()
}