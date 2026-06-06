package com.example.myapplication.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.domain.model.AppUser
import com.example.myapplication.domain.model.MovementType
import com.example.myapplication.domain.model.StockMovement
import com.example.myapplication.domain.model.UserRole

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    currentUser: AppUser? = null,
    onLogout: () -> Unit = {},
    onNavigateCatalog: () -> Unit = {},
    onNavigateScanner: () -> Unit = {},
    onNavigateReports: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showUsersDialog by remember { mutableStateOf(false) }

    if (showUsersDialog) {
        UsersDialog(
            users = uiState.users,
            onDismiss = { showUsersDialog = false }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "StickrVault",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (currentUser != null) "Bienvenido, ${currentUser.name}"
                        else "Gestión de inventario PANINI",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    currentUser?.let { RoleBadge(role = it.role) }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar sesión",
                            tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    MetricCard("Total Productos", uiState.totalProducts.toString(),
                        Icons.Default.Inventory, MaterialTheme.colorScheme.primary)
                }
                item {
                    MetricCard("Stock Crítico", uiState.criticalStockProducts.toString(),
                        Icons.Default.Warning, MaterialTheme.colorScheme.error)
                }
                item {
                    MetricCard("Pendientes Sync", uiState.pendingSyncItems.toString(),
                        Icons.Default.Sync, MaterialTheme.colorScheme.secondary)
                }
            }
        }

        item {
            Text("Acceso Rápido", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickAccessCard(Modifier.weight(1f), "Catálogo",  Icons.Default.Category,      onNavigateCatalog)
                QuickAccessCard(Modifier.weight(1f), "Escáner",   Icons.Default.QrCodeScanner, onNavigateScanner)
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickAccessCard(Modifier.weight(1f), "Reportes", Icons.Default.Assessment, onNavigateReports)
                if (currentUser?.role == UserRole.WAREHOUSE_CHIEF) {
                    QuickAccessCard(Modifier.weight(1f), "Usuarios", Icons.Default.People,
                        onClick = { showUsersDialog = true })
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text("Actividad Reciente", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
                Icon(Icons.Default.ChevronRight, contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        if (uiState.recentMovements.isEmpty()) {
            item {
                Text("Sin actividad reciente", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            items(uiState.recentMovements) { RecentMovementItem(it) }
        }
    }
}

@Composable
fun UsersDialog(users: List<AppUser>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Gestión de Usuarios", fontWeight = FontWeight.Bold) },
        text = {
            if (users.isEmpty()) {
                Text("Cargando usuarios...", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    users.forEach { user ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(user.name, style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold)
                                    Text(user.email, style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                RoleBadge(role = user.role)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}

@Composable
fun MetricCard(title: String, value: String, icon: ImageVector, color: Color) {
    Card(modifier = Modifier.width(140.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold, color = color)
            Text(title, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun QuickAccessCard(modifier: Modifier = Modifier, label: String, icon: ImageVector, onClick: () -> Unit) {
    Card(modifier = modifier, onClick = onClick) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun RecentMovementItem(movement: StockMovement) {
    val (quantityText, quantityColor) = when (movement.movementType) {
        MovementType.ENTRY      -> "+${movement.quantity}" to Color(0xFF2E7D32)
        MovementType.EXIT       -> "-${movement.quantity}" to MaterialTheme.colorScheme.error
        MovementType.ADJUSTMENT -> "~${movement.quantity}" to MaterialTheme.colorScheme.primary
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(movement.productId, style = MaterialTheme.typography.titleSmall)
                Text("${movement.userName} · ${formatRelativeTime(movement.timestamp)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(quantityText, color = quantityColor,
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

fun formatRelativeTime(timestamp: Long): String {
    val diff    = System.currentTimeMillis() - timestamp
    val minutes = diff / 60_000
    val hours   = minutes / 60
    val days    = hours / 24
    return when {
        minutes < 1  -> "Ahora mismo"
        minutes < 60 -> "Hace $minutes min"
        hours < 24   -> "Hace ${hours}h"
        else         -> "Hace $days días"
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
    Surface(color = color.copy(alpha = 0.15f), shape = MaterialTheme.shapes.small) {
        Text(label, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall, color = color)
    }
}

@Composable
fun SummaryCard(title: String, value: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}