package com.example.myapplication.presentation.scanner

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ScannerViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<ScannerUiState>(
        ScannerUiState.Idle
    )

    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    fun startScanning() {
        _uiState.value = ScannerUiState.Scanning
    }

    fun detectProduct(identifier: String) {
        _uiState.value = ScannerUiState.ProductDetected(identifier)
    }

    fun stopScanning() {
        _uiState.value = ScannerUiState.Idle
    }
}