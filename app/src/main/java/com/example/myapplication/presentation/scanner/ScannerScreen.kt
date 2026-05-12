package com.example.myapplication.presentation.scanner

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ScannerScreen(
    viewModel: ScannerViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Escáner de productos",
            style = MaterialTheme.typography.headlineSmall
        )

        when (uiState) {
            is ScannerUiState.Idle -> {
                Text("Escáner en espera")
            }

            is ScannerUiState.Scanning -> {
                Text("Escaneando producto...")
            }

            is ScannerUiState.ProductDetected -> {
                val state = uiState as ScannerUiState.ProductDetected
                Text("Producto detectado: ${state.identifier}")
            }

            is ScannerUiState.Error -> {
                val state = uiState as ScannerUiState.Error
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        Button(
            onClick = { viewModel.startScanning() }
        ) {
            Text("Iniciar escaneo")
        }

        Button(
            onClick = { viewModel.detectProduct("ARG10") }
        ) {
            Text("Simular detección")
        }

        OutlinedButton(
            onClick = { viewModel.stopScanning() }
        ) {
            Text("Detener")
        }
    }
}