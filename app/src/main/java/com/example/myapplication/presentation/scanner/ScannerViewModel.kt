package com.example.myapplication.presentation.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.usecase.GetProductByOcrIdentifierUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScannerViewModel(
    private val getProductByOcrIdentifier: GetProductByOcrIdentifierUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScannerUiState>(ScannerUiState.Idle)
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    private var lastScannedText = ""

    fun onTextDetected(rawText: String) {
        val identifier = rawText.trim()
        if (identifier.isBlank() || identifier == lastScannedText) return
        lastScannedText = identifier
        _uiState.value = ScannerUiState.ProductDetected(identifier)
        viewModelScope.launch {
            try {
                val product = getProductByOcrIdentifier(identifier)
                _uiState.value = if (product != null)
                    ScannerUiState.ProductFound(product)
                else
                    ScannerUiState.ProductNotFound(identifier)
            } catch (e: Exception) {
                _uiState.value = ScannerUiState.Error(e.message ?: "Error al buscar producto")
            }
        }
    }

    fun startScanning() {
        lastScannedText = ""
        _uiState.value = ScannerUiState.Scanning
    }

    fun stopScanning() { _uiState.value = ScannerUiState.Idle }

    fun reset() {
        lastScannedText = ""
        _uiState.value = ScannerUiState.Idle
    }
}