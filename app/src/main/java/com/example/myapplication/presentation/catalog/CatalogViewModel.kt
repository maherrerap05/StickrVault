package com.example.myapplication.presentation.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.Product
import com.example.myapplication.domain.model.ProductCategory
import com.example.myapplication.domain.usecase.AddProductUseCase
import com.example.myapplication.domain.usecase.AddStockMovementUseCase
import com.example.myapplication.domain.usecase.FilterProductsByCategoryUseCase
import com.example.myapplication.domain.usecase.GetProductByNameAndCategoryUseCase
import com.example.myapplication.domain.usecase.GetProductsUseCase
import com.example.myapplication.domain.usecase.SearchProductsUseCase
import com.example.myapplication.domain.usecase.UpdateProductUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import com.example.myapplication.domain.model.AppUser
import com.example.myapplication.domain.model.MovementType
import com.example.myapplication.domain.model.StockMovement

class CatalogViewModel(
    private val getProductsUseCase: GetProductsUseCase,
    private val searchProductsUseCase: SearchProductsUseCase,
    private val filterProductsByCategoryUseCase: FilterProductsByCategoryUseCase,
    private val addProductUseCase: AddProductUseCase,
    private val getProductByNameAndCategoryUseCase: GetProductByNameAndCategoryUseCase,
    private val updateProductUseCase: UpdateProductUseCase,
    private val addStockMovementUseCase: AddStockMovementUseCase

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


    fun saveManualProduct(
        name: String,
        category: ProductCategory,
        stockValue: Int,
        minimumStock: Int,
        ocrIdentifier: String?,
        currentUser: AppUser?
    ) {
        viewModelScope.launch {
            _uiState.value = CatalogUiState.Loading

            try {
                val cleanName = name.trim()
                val existingProduct = getProductByNameAndCategoryUseCase(cleanName, category)

                val savedProduct: Product?

                if (existingProduct != null) {
                    val newStock = existingProduct.currentStock + stockValue

                    if (newStock < 0) {
                        _uiState.value = CatalogUiState.Error(
                            "No se puede deducir más stock del disponible."
                        )
                        return@launch
                    }

                    val updatedProduct = existingProduct.copy(
                        currentStock = newStock,
                        lastUpdated = System.currentTimeMillis(),
                        isSynced = false
                    )

                    savedProduct = updateProductUseCase(updatedProduct)

                    addStockMovementUseCase(
                        StockMovement(
                            id = UUID.randomUUID().toString(),
                            productId = existingProduct.id,
                            movementType = if (stockValue >= 0) MovementType.ENTRY else MovementType.EXIT,
                            quantity = kotlin.math.abs(stockValue),
                            userId = currentUser?.id ?: "offline-user",
                            userName = currentUser?.name ?: "Usuario offline",
                            timestamp = System.currentTimeMillis(),
                            isSynced = false
                        )
                    )

                } else {
                    val newProduct = Product(
                        id = UUID.randomUUID().toString(),
                        name = cleanName,
                        category = category,
                        description = "Producto registrado en bodega",
                        currentStock = stockValue,
                        minimumStock = minimumStock,
                        imageUrl = null,
                        ocrIdentifier = ocrIdentifier?.ifBlank { null },
                        lastUpdated = System.currentTimeMillis(),
                        isSynced = false
                    )

                    savedProduct = addProductUseCase(newProduct)

                    savedProduct?.let {
                        addStockMovementUseCase(
                            StockMovement(
                                id = UUID.randomUUID().toString(),
                                productId = it.id,
                                movementType = MovementType.ENTRY,
                                quantity = stockValue,
                                userId = currentUser?.id ?: "offline-user",
                                userName = currentUser?.name ?: "Usuario offline",
                                timestamp = System.currentTimeMillis(),
                                isSynced = false
                            )
                        )
                    }
                }

                if (savedProduct != null) {
                    val products = getProductsUseCase()
                    val pending = products.count { !it.isSynced }

                    _uiState.value = CatalogUiState.Success(
                        products = products,
                        isOffline = pending > 0,
                        pendingSyncCount = pending,
                        message = if (existingProduct != null)
                            "Stock actualizado correctamente."
                        else
                            "Producto agregado correctamente."
                    )
                } else {
                    _uiState.value = CatalogUiState.Error("No se pudo guardar el producto.")
                }

            } catch (e: Exception) {
                _uiState.value = CatalogUiState.Error(
                    e.message ?: "Error al guardar producto"
                )
            }
        }
    }
}