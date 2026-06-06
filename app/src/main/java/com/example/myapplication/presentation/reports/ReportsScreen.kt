package com.example.myapplication.presentation.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.domain.model.MovementType
import com.example.myapplication.domain.model.StockMovement

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

        Text(
            text = "Movimientos recientes",
            style = MaterialTheme.typography.titleMedium
        )

        if (uiState.recentMovements.isEmpty()) {
            Text(
                text = "Sin movimientos registrados",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.recentMovements) { movement ->
                    MovementCard(movement)
                }
            }
        }
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

@Composable
fun MovementCard(movement: StockMovement) {
    val quantityText = when (movement.movementType) {
        MovementType.ENTRY      -> "+${movement.quantity}"
        MovementType.EXIT       -> "-${movement.quantity}"
        MovementType.ADJUSTMENT -> "~${movement.quantity}"
    }
    val quantityColor = when (movement.movementType) {
        MovementType.ENTRY      -> Color(0xFF2E7D32)
        MovementType.EXIT       -> MaterialTheme.colorScheme.error
        MovementType.ADJUSTMENT -> MaterialTheme.colorScheme.primary
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = movement.productId,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = movement.userName,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = quantityText,
                color = quantityColor,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}