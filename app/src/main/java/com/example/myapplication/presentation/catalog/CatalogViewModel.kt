package com.example.myapplication.presentation.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.Product
import com.example.myapplication.domain.model.ProductCategory
import com.example.myapplication.domain.usecase.AddProductUseCase
import com.example.myapplication.domain.usecase.FilterProductsByCategoryUseCase
import com.example.myapplication.domain.usecase.GetProductsUseCase
import com.example.myapplication.domain.usecase.SearchProductsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class CatalogViewModel(
    private val getProductsUseCase: GetProductsUseCase,
    private val searchProductsUseCase: SearchProductsUseCase,
    private val filterProductsByCategoryUseCase: FilterProductsByCategoryUseCase,
    private val addProductUseCase: AddProductUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<CatalogUiState>(CatalogUiState.Loading)
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    init { loadProducts() }

    fun loadProducts() {
        viewModelScope.launch {
            _uiState.value = CatalogUiState.Loading
            try {
                val products = getProductsUseCase()
                _uiState.value = if (products.isEmpty()) CatalogUiState.Empty
                else CatalogUiState.Success(products = products)
            } catch (e: Exception) {
                _uiState.value = CatalogUiState.Error(e.message ?: "Error al cargar productos")
            }
        }
    }

    fun searchProducts(query: String) {
        viewModelScope.launch {
            _uiState.value = CatalogUiState.Loading
            try {
                val products = searchProductsUseCase(query)
                _uiState.value = if (products.isEmpty()) CatalogUiState.Empty
                else CatalogUiState.Success(products = products, searchQuery = query)
            } catch (e: Exception) {
                _uiState.value = CatalogUiState.Error(e.message ?: "Error al buscar")
            }
        }
    }

    fun filterByCategory(category: ProductCategory) {
        viewModelScope.launch {
            _uiState.value = CatalogUiState.Loading
            try {
                val products = filterProductsByCategoryUseCase(category)
                _uiState.value = if (products.isEmpty()) CatalogUiState.Empty
                else CatalogUiState.Success(products = products, activeFilter = category)
            } catch (e: Exception) {
                _uiState.value = CatalogUiState.Error(e.message ?: "Error al filtrar")
            }
        }
    }

    fun addProduct(
        name: String,
        category: ProductCategory,
        currentStock: Int,
        minimumStock: Int,
        ocrIdentifier: String?
    ) {
        viewModelScope.launch {
            _uiState.value = CatalogUiState.Loading

            try {
                val product = Product(
                    id = UUID.randomUUID().toString(),
                    name = name.trim(),
                    category = category,
                    description = "Producto registrado en bodega",
                    currentStock = currentStock,
                    minimumStock = minimumStock,
                    imageUrl = null,
                    ocrIdentifier = ocrIdentifier?.ifBlank { null },
                    lastUpdated = System.currentTimeMillis(),
                    isSynced = false
                )

                val savedProduct = addProductUseCase(product)

                if (savedProduct != null) {
                    val products = getProductsUseCase()
                    val pending = products.count { !it.isSynced }

                    _uiState.value = CatalogUiState.Success(
                        products = products,
                        isOffline = !savedProduct.isSynced,
                        pendingSyncCount = pending,
                        message = if (!savedProduct.isSynced)
                            "Modo offline: producto guardado localmente."
                        else
                            "Producto sincronizado correctamente."
                    )
                } else {
                    _uiState.value = CatalogUiState.Error("No se pudo agregar el producto.")
                }

            } catch (e: Exception) {
                _uiState.value = CatalogUiState.Error(
                    e.message ?: "Error al agregar producto"
                )
            }
        }
    }
}