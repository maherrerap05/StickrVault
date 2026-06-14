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

    private var lastScannedCode = ""

    fun openCamera() {
        lastScannedCode = ""
        _uiState.value = ScannerUiState.CameraReady
    }

    fun onCaptureRequested() {
        if (_uiState.value is ScannerUiState.CameraReady) {
            _uiState.value = ScannerUiState.ProcessingCapture
        }
    }

    fun onCaptureResult(rawText: String, extractedCode: String?) {
        val code = extractedCode?.trim().orEmpty()
        if (code.isBlank()) {
            _uiState.value = ScannerUiState.CodeNotRecognized(rawText.take(120))
            return
        }
        if (code == lastScannedCode) {
            _uiState.value = ScannerUiState.CameraReady
            return
        }

        lastScannedCode = code
        _uiState.value = ScannerUiState.Searching(code)
        viewModelScope.launch {
            try {
                val product = getProductByOcrIdentifier(code)
                _uiState.value = if (product != null) {
                    ScannerUiState.ProductFound(product, code)
                } else {
                    ScannerUiState.ProductNotFound(code)
                }
            } catch (e: Exception) {
                _uiState.value = ScannerUiState.Error(e.message ?: "Error al buscar producto")
            }
        }
    }

    fun backToCamera() {
        _uiState.value = ScannerUiState.CameraReady
    }

    fun closeCamera() {
        _uiState.value = ScannerUiState.Idle
    }

    fun reset() {
        lastScannedCode = ""
        _uiState.value = ScannerUiState.Idle
    }
}
