package com.example.myapplication.presentation.reports

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Reportes de inventario",
            style = MaterialTheme.typography.headlineSmall
        )

        ReportMetricCard(
            title = "Total de productos",
            value = uiState.totalProducts.toString()
        )

        ReportMetricCard(
            title = "Total de movimientos",
            value = uiState.totalMovements.toString()
        )

        ReportMetricCard(
            title = "Productos con stock crítico",
            value = uiState.criticalStockProducts.toString()
        )

        ReportMetricCard(
            title = "Producto con mayor stock",
            value = uiState.mostStockedProductName
        )
    }
}

@Composable
fun ReportMetricCard(
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