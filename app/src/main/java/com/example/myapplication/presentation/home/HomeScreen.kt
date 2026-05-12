package com.example.myapplication.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    viewModel: HomeViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Bienvenido a StickrVault",
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = "Resumen general del inventario PANINI Mundial 2026",
            style = MaterialTheme.typography.bodyMedium
        )

        SummaryCard(
            title = "Total de productos",
            value = uiState.totalProducts.toString()
        )

        SummaryCard(
            title = "Productos con stock crítico",
            value = uiState.criticalStockProducts.toString()
        )

        SummaryCard(
            title = "Cambios pendientes de sincronización",
            value = uiState.pendingSyncItems.toString()
        )

        SummaryCard(
            title = "Última sincronización",
            value = uiState.lastSyncText
        )
    }
}

@Composable
fun SummaryCard(
    title: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}