package com.example.myapplication.presentation.catalog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.domain.model.AppUser
import com.example.myapplication.domain.model.Product
import com.example.myapplication.domain.model.ProductCategory
import com.example.myapplication.domain.model.UserRole

@Composable
fun CatalogScreen(
    viewModel: CatalogViewModel,
    currentUser: AppUser? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val addProductDraft by viewModel.addProductDraft.collectAsState()
    val isAddProductDialogVisible by viewModel.isAddProductDialogVisible.collectAsState()
    var searchText by remember { mutableStateOf("") }

    val canEdit = currentUser?.role != UserRole.AUDITOR
    val successState = uiState as? CatalogUiState.Success
    val catalogProducts by viewModel.catalogProducts.collectAsState()

    if (isAddProductDialogVisible) {
        AddProductDialog(
            draft = addProductDraft,
            products = catalogProducts,
            onDismiss = viewModel::dismissAddProductDialog,
            onCancel = viewModel::cancelAddProductDialog,
            onDraftChange = viewModel::updateAddProductDraft,
            onVerify = viewModel::verifyAddProductDraft,
            onConfirm = { name, category, stock, minStock, ocrId ->
                viewModel.saveManualProduct(
                    name = name,
                    category = category,
                    stockValue = stock,
                    minimumStock = minStock,
                    ocrIdentifier = ocrId,
                    currentUser = currentUser
                )
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            if (canEdit) {
                FloatingActionButton(onClick = viewModel::openAddProductDialog) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar producto")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Catálogo de Inventario",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchText,
                onValueChange = {
                    searchText = it
                    if (it.isBlank()) {
                        viewModel.loadProducts()
                    } else {
                        viewModel.searchProducts(it)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Buscar producto") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (successState?.isOffline == true || (successState?.pendingSyncCount ?: 0) > 0) {
                AssistChip(
                    onClick = {},
                    label = {
                        Text("Modo offline · Pendientes: ${successState?.pendingSyncCount ?: 0}")
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            successState?.message?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            val activeFilter = successState?.activeFilter

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = activeFilter == null,
                        onClick = {
                            searchText = ""
                            viewModel.loadProducts()
                        },
                        label = { Text("Todos") }
                    )
                }

                items(ProductCategory.entries) { category ->
                    FilterChip(
                        selected = activeFilter == category,
                        onClick = {
                            searchText = ""
                            viewModel.filterByCategory(category)
                        },
                        label = { Text(category.displayName()) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            when (val state = uiState) {
                is CatalogUiState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

                is CatalogUiState.Empty -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay productos disponibles",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                is CatalogUiState.Error -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                is CatalogUiState.Success -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(state.products) { product ->
                        ProductCard(product = product, canEdit = canEdit)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductDialog(
    draft: AddProductDraft,
    products: List<Product>,
    onDismiss: () -> Unit,
    onCancel: () -> Unit,
    onDraftChange: ((AddProductDraft) -> AddProductDraft) -> Unit,
    onVerify: () -> Unit,
    onConfirm: (name: String, category: ProductCategory, stock: Int, minStock: Int, ocrId: String) -> Unit
) {
    val name = draft.name
    val category = draft.category
    val stock = draft.stock
    val minStock = draft.minStock
    val ocrId = draft.ocrId
    val wasVerified = draft.wasVerified
    val existingProduct = draft.existingProductId?.let { id ->
        products.firstOrNull { it.id == id }
    }

    var expanded by remember { mutableStateOf(false) }

    val isExistingProduct = wasVerified && existingProduct != null
    val isNewProduct = wasVerified && existingProduct == null

    val stockValue = stock.toIntOrNull()
    val minStockValue = minStock.toIntOrNull()

    val resultingStock = if (isExistingProduct && stockValue != null) {
        existingProduct!!.currentStock + stockValue
    } else {
        stockValue
    }

    val canVerify = name.isNotBlank()

    val isFormValid = when {
        isExistingProduct -> {
            stockValue != null &&
                    stockValue != 0 &&
                    resultingStock != null &&
                    resultingStock >= 0
        }

        isNewProduct -> {
            stockValue != null &&
                    minStockValue != null &&
                    stockValue > 0 &&
                    minStockValue >= 0 &&
                    minStockValue < stockValue
        }

        else -> false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = when {
                    !wasVerified -> "Verificar Producto"
                    isExistingProduct -> "Ajustar Stock"
                    else -> "Agregar Producto Nuevo"
                },
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                OutlinedTextField(
                    value = name,
                    onValueChange = { value ->
                        onDraftChange {
                            it.copy(
                                name = value,
                                wasVerified = false,
                                existingProductId = null,
                                stock = ""
                            )
                        }
                    },
                    label = { Text("Nombre del producto") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = category.displayName(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        ProductCategory.entries.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.displayName()) },
                                onClick = {
                                    onDraftChange {
                                        it.copy(
                                            category = cat,
                                            wasVerified = false,
                                            existingProductId = null,
                                            stock = ""
                                        )
                                    }
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                if (!wasVerified) {
                    Button(
                        onClick = onVerify,
                        enabled = canVerify,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Verificar existencia")
                    }
                }

                if (isExistingProduct) {
                    AssistChip(
                        onClick = {},
                        label = {
                            Text("Producto existente · Stock actual: ${existingProduct!!.currentStock}")
                        }
                    )

                    OutlinedTextField(
                        value = stock,
                        onValueChange = { value -> onDraftChange { it.copy(stock = value) } },
                        label = { Text("Cantidad a modificar") },
                        placeholder = { Text("Ej: 10 o -5") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                    )

                    Text(
                        text = "Usa valores positivos para aumentar stock y negativos para reducirlo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (stockValue != null) {
                        Text(
                            text = "Stock resultante: $resultingStock",
                            style = MaterialTheme.typography.bodySmall,
                            color = if ((resultingStock ?: 0) < 0)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.primary
                        )
                    }

                    if ((resultingStock ?: 0) < 0) {
                        Text(
                            text = "No puedes dejar el stock en negativo.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                if (isNewProduct) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Producto no encontrado · Se creará uno nuevo") }
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = stock,
                            onValueChange = { value -> onDraftChange { it.copy(stock = value) } },
                            label = { Text("Stock inicial") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        OutlinedTextField(
                            value = minStock,
                            onValueChange = { value -> onDraftChange { it.copy(minStock = value) } },
                            label = { Text("Stock mínimo") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }

                    if (stockValue != null && minStockValue != null && minStockValue >= stockValue) {
                        Text(
                            text = "El stock mínimo debe ser menor que el stock inicial.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    OutlinedTextField(
                        value = ocrId,
                        onValueChange = { value -> onDraftChange { it.copy(ocrId = value) } },
                        label = { Text("ID OCR (opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        name,
                        category,
                        stockValue ?: 0,
                        minStockValue ?: existingProduct?.minimumStock ?: 0,
                        ocrId
                    )
                },
                enabled = isFormValid
            ) {
                Text(
                    when {
                        isExistingProduct -> "Actualizar stock"
                        isNewProduct -> "Agregar producto"
                        else -> "Verifica primero"
                    }
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun ProductCard(product: Product, canEdit: Boolean = true) {
    val isCritical = product.currentStock <= product.minimumStock
    val stockColor = if (isCritical) MaterialTheme.colorScheme.error else Color(0xFF2E7D32)
    val stockLabel = if (isCritical) "● Crítico" else "● Normal"

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = product.category.displayName(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (!product.isSynced) {
                    Text(
                        text = "Pendiente de sincronización",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = product.currentStock.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = stockLabel,
                    color = stockColor,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

private fun ProductCategory.displayName() = when (this) {
    ProductCategory.STICKER_INDIVIDUAL -> "Cromos"
    ProductCategory.STICKER_PACK -> "Packs"
    ProductCategory.ALBUM -> "Álbumes"
    ProductCategory.STICKER_BOX -> "Cajas"
    ProductCategory.PLUSH -> "Peluches"
    ProductCategory.BALL -> "Balones"
    ProductCategory.SPECIAL_EDITION -> "Edición Especial"
}