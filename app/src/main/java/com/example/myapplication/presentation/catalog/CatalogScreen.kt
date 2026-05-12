package com.example.myapplication.presentation.catalog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.domain.model.Product
import com.example.myapplication.domain.model.ProductCategory

@Composable
fun CatalogScreen(
    viewModel: CatalogViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Catálogo de productos",
            style = MaterialTheme.typography.headlineSmall
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
            label = {
                Text("Buscar producto")
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        CategoryFilters(
            onAllClick = {
                searchText = ""
                viewModel.loadProducts()
            },
            onCategoryClick = { category ->
                searchText = ""
                viewModel.filterByCategory(category)
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        when (uiState) {
            is CatalogUiState.Loading -> LoadingContent()
            is CatalogUiState.Empty -> EmptyContent()
            is CatalogUiState.Error -> {
                val errorState = uiState as CatalogUiState.Error
                ErrorContent(message = errorState.message)
            }
            is CatalogUiState.Success -> {
                val successState = uiState as CatalogUiState.Success
                ProductList(products = successState.products)
            }
        }
    }
}

@Composable
fun CategoryFilters(
    onAllClick: () -> Unit,
    onCategoryClick: (ProductCategory) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            AssistChip(
                onClick = onAllClick,
                label = { Text("Todos") }
            )
        }

        items(ProductCategory.entries) { category ->
            AssistChip(
                onClick = { onCategoryClick(category) },
                label = { Text(category.name) }
            )
        }
    }
}

@Composable
fun LoadingContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun EmptyContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "No hay productos disponibles")
    }
}

@Composable
fun ErrorContent(
    message: String
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
fun ProductList(
    products: List<Product>
) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(products) { product ->
            ProductCard(product)
        }
    }
}

@Composable
fun ProductCard(
    product: Product
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Categoría: ${product.category.name}"
            )

            Text(
                text = "Stock: ${product.currentStock}"
            )

            if (product.currentStock <= product.minimumStock) {
                Text(
                    text = "Stock bajo",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}