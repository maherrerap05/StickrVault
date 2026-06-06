package com.example.myapplication.presentation.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.domain.model.MovementType
import com.example.myapplication.domain.model.StockMovement
import com.example.myapplication.presentation.home.formatRelativeTime

@Composable
fun ReportsScreen(viewModel: ReportsViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Reportes de Inventario",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        // ── Grid 2 columnas de métricas ───────────────────────
        item {
            val metrics = listOf(
                "Total Productos"     to uiState.totalProducts.toString(),
                "Movimientos"         to uiState.totalMovements.toString(),
                "Stock Crítico"       to uiState.criticalStockProducts.toString(),
                "Mayor Stock"         to uiState.mostStockedProductName
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.height(220.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                userScrollEnabled = false
            ) {
                items(metrics) { (title, value) ->
                    ReportMetricCard(title = title, value = value)
                }
            }
        }

        // ── Movimientos recientes ─────────────────────────────
        item {
            Text(
                text = "Movimientos Recientes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (uiState.recentMovements.isEmpty()) {
            item {
                Text(
                    text = "Sin movimientos registrados",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(uiState.recentMovements) { movement ->
                MovementCard(movement)
            }
        }
    }
}

@Composable
fun ReportMetricCard(title: String, value: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MovementCard(movement: StockMovement) {
    val (quantityText, quantityColor) = when (movement.movementType) {
        MovementType.ENTRY      -> "↑ +${movement.quantity}" to Color(0xFF2E7D32)
        MovementType.EXIT       -> "↓ -${movement.quantity}" to MaterialTheme.colorScheme.error
        MovementType.ADJUSTMENT -> "~ ${movement.quantity}"  to MaterialTheme.colorScheme.primary
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(movement.productId, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    text = "${movement.userName} · ${formatRelativeTime(movement.timestamp)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(quantityText, color = quantityColor, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}