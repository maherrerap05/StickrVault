package com.example.myapplication.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.domain.model.AppUser
import com.example.myapplication.domain.model.UserRole

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    currentUser: AppUser? = null,
    onLogout: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (currentUser != null) "Bienvenido, ${currentUser.name}"
                    else "Bienvenido a StickrVault",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "Inventario PANINI Mundial 2026",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                currentUser?.let { RoleBadge(role = it.role) }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onLogout) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Cerrar sesión",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        SummaryCard(title = "Total de productos",                    value = uiState.totalProducts.toString())
        SummaryCard(title = "Productos con stock crítico",           value = uiState.criticalStockProducts.toString())
        SummaryCard(title = "Cambios pendientes de sincronización",  value = uiState.pendingSyncItems.toString())
        SummaryCard(title = "Última sincronización",                 value = uiState.lastSyncText)
    }
}

@Composable
fun RoleBadge(role: UserRole) {
    val label = when (role) {
        UserRole.WAREHOUSE_CHIEF    -> "Jefe de Bodega"
        UserRole.WAREHOUSE_OPERATOR -> "Operador"
        UserRole.AUDITOR            -> "Auditor"
    }
    val color = when (role) {
        UserRole.WAREHOUSE_CHIEF    -> MaterialTheme.colorScheme.primary
        UserRole.WAREHOUSE_OPERATOR -> MaterialTheme.colorScheme.secondary
        UserRole.AUDITOR            -> MaterialTheme.colorScheme.tertiary
    }
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@Composable
fun SummaryCard(title: String, value: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}